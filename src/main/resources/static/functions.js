var subscription = null;
var newQuery = 0;

function registerTemplate() {
	template = $("#template").html();
	Mustache.parse(template);
}

function setConnected(connected) {
	var search = $('#submitsearch');
	search.prop('disabled', !connected);



}

function registerSendQueryAndConnect() {
    console.log("hola2");
    var socket = new SockJS("/twitter");
    var stompClient = Stomp.over(socket);

    stompClient.connect({}, function(frame) {
        setConnected(true);
        console.log('Connected: ' + frame);

        subscriptionTopics = stompClient.subscribe("/queue/trends", function (data) {

            var resultsBlock = $("#resultsTopics");
			var topics = JSON.parse(data.body);
			console.log(topics);

			resultsBlock.empty();
			for (var i in topics) {
				for (var topic in topics[i]) {
                    resultsBlock.append("<li class=\"list-group-item d-flex justify-content-between align-items-center\">"
                        +topic+"<span class=\"badge badge-primary badge-pill\">"+topics[i][topic]+"</span>"+"</li>");


				}
			}
        });


    });

	$("#search").submit(
			function(event) {
				event.preventDefault();
				if (subscription) {
					subscription.unsubscribe();
				}
				var query = $("#q").val();
				stompClient.send("/app/search", {}, query);
				newQuery = 1;
				subscription = stompClient.subscribe("/queue/search/" + query, function(data) {
					var resultsBlock = $("#resultsBlock");
					if (newQuery) {
                        resultsBlock.empty();
						newQuery = 0;
					}
					var tweet = JSON.parse(data.body);
                    resultsBlock.prepend(Mustache.render(template, tweet));
				});
			});
}

$(document).ready(function() {
    console.log("hola1");
	registerTemplate();
	registerSendQueryAndConnect();
});
