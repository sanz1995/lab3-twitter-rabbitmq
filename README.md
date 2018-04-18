## Laboratorio 3 : Desarrollar un flujo cliente de RabbitMQ

Se os proporciona una versión del servidor que por defecto un flujo denominado #1 
se conecta al stream de todos los Tweets geolocalizados y los envía a un exchange RabbitMQ 
que los enruta a la queue #A.

Un flujo #2 se conecta al la queue #A de RabbitMQ y procesa los mensajes como en el 
laboratorio 2.


Ya se ha creado:
 * La infraestructura en el cliente Web para subscribirse a al canal `/queue/trends 
    que recibirá trending topics
 * El método `StreamSendingService#sendTrends()` que se encargará de enviar mensajes 
   a todos los los clientes suscritos a `/queue/trends`
 * El código JavaScript necesario para recibir los mensajes y convertirlos en HTML


### Preparación

* Configurar un servidor de RabbitMQ local, por ejemplo con Docker.
```
docker run -d --hostname my-rabbit -p 5672:5672 --name some-rabbit rabbitmq:3
```


### Objetivos

* Hay que crear un flujo #3 que procese exactamente los mismos mensajes que el flujo #2 leyéndolos de RabbitMQ
* El flujo #3 debe ser capaz de agregarlos y crear una lista de un máximo de 10 pares (key, value) de trending topics ordenados de mayor a menor valor
  
* Primera decisión: ¿qué tipo de exchange es el más adecuado? Implica definir profile activo
* Segunda decisión: ¿dónde implemento el flujo #3?


