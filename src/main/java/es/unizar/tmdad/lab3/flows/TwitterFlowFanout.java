package es.unizar.tmdad.lab3.flows;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.integration.amqp.inbound.AmqpInboundChannelAdapter;
import org.springframework.integration.amqp.outbound.AmqpOutboundEndpoint;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.dsl.AggregatorSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.amqp.Amqp;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.dsl.support.Consumer;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.social.twitter.api.HashTagEntity;
import org.springframework.social.twitter.api.Tweet;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@Profile("fanout")
public class TwitterFlowFanout extends TwitterFlowCommon {

	final static String TWITTER_FANOUT_EXCHANGE = "twitter_fanout";
	final static String TWITTER_FANOUT_A_QUEUE_NAME = "twitter_fanout_queue";

	@Autowired
	RabbitTemplate rabbitTemplate;

	// Configuración RabbitMQ

	@Bean
	Queue aTwitterFanoutQueue() {
		return new Queue(TWITTER_FANOUT_A_QUEUE_NAME, false);
	}

	@Bean
	FanoutExchange twitterFanoutExchange() {
		return new FanoutExchange(TWITTER_FANOUT_EXCHANGE);
	}

	@Bean
	Binding twitterFanoutBinding() {
		return BindingBuilder.bind(aTwitterFanoutQueue()).to(
				twitterFanoutExchange());
	}

	// Flujo #1
	//
	// MessageGateway Twitter -(requestChannelTwitter)-> MessageEndpoint
	// RabbitMQ
	//

	@Bean
	public DirectChannel requestChannelTwitter() {
		return MessageChannels.direct().get();
	}

	@Bean
	public AmqpOutboundEndpoint amqpOutbound() {
		return Amqp.outboundAdapter(rabbitTemplate)
				.exchangeName(TWITTER_FANOUT_EXCHANGE).get();
	}

	@Bean
	public IntegrationFlow sendTweetToRabbitMQ() {
		return IntegrationFlows.from(requestChannelTwitter())
				.handle(amqpOutbound()).get();
	}

	// Flujo #2
	//
	// MessageEndpoint RabbitMQ -(requestChannelRabbitMQ)-> tareas ...
	//

	@Override
	@Bean
	public DirectChannel requestChannelRabbitMQ() {
		return MessageChannels.direct().get();
	}


	@Bean
	public AmqpInboundChannelAdapter amqpInbound() {
		SimpleMessageListenerContainer smlc = new SimpleMessageListenerContainer(
				rabbitTemplate.getConnectionFactory());
		smlc.setQueues(aTwitterFanoutQueue());
		return Amqp.inboundAdapter(smlc)
				.outputChannel(requestChannelRabbitMQ()).get();
	}




	//Flujo 3


	@Bean
	public IntegrationFlow sendTopics() {
		return IntegrationFlows
				.from(requestChannelRabbitMQ())
				.filter("payload instanceof T(org.springframework.social.twitter.api.Tweet)")
				.aggregate(aggregationSpec())
				.transform(trendingTopics())
				.handle("streamSendingService", "sendTrends").get();
	}

	private Consumer<AggregatorSpec> aggregationSpec() {
		return a -> a.correlationStrategy(m -> 1)
				.releaseStrategy(g -> g.size() == 1000)
				.expireGroupsUponCompletion(true);
	}

	private GenericTransformer<List<Tweet>, List<Map.Entry<String, Integer>>> trendingTopics() {
		return lista -> {
			Map<String, Integer> codes = lista.stream()
					.flatMap(t -> t.getEntities().getHashTags().stream())
					.collect(Collectors.groupingBy(HashTagEntity::getText, Collectors.reducing(0, s -> 1, Integer::sum)));

			List<Map.Entry<String, Integer>> topics = new ArrayList<>(codes.entrySet());


			topics.sort(Collections.reverseOrder(Map.Entry.comparingByValue()));

			return topics.subList(0, 10);
		};
	}

}