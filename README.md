## Laboratorio 3 : Desarrollar un flujo cliente de RabbitMQ

Se os proporciona una versión del servidor que por defecto un _Flow #1_ 
se conecta al stream de todos los Tweets geolocalizados y los envía a un exchange RabbitMQ 
que los enruta a la _Queue #A_.

Un  _Flow #2_ se conecta al la _Queue #A_ de RabbitMQ y procesa los mensajes como en el 
laboratorio 2.


Ya se ha creado:
 * La infraestructura en el cliente Web para subscribirse a al canal `/queue/trends` que recibirá trending topics
 * El método `StreamSendingService#sendTrends()` que se encargará de enviar mensajes a todos los los clientes suscritos a `/queue/trends`
 * El código JavaScript necesario para recibir los mensajes y convertirlos en HTML


### Preparación

* Configurar un servidor de RabbitMQ local, por ejemplo con Docker.
```
docker run -d --hostname my-rabbit -p 5672:5672 --name some-rabbit rabbitmq:3
```


### Objetivos

Hay que crear un _Flow #3_ que procese exactamente los mismos mensajes que el _Flow #2_ leyéndolos de RabbitMQ. El _Flow #3_ debe ser capaz de agregarlos y crear una lista de un máximo de 10 pares (key, value) de trending topics ordenados de mayor a menor valor
  
_Primera decisión_: ¿Qué tipo de exchange es el más adecuado? Implica indicar `profile` de Spring Boot activo. Ejemplo:
```
SPRING_PROFILES_ACTIVE=direct ./gradlew bootRun
```
_Segunda decisión_: ¿Dónde implemento el _Flow #3_?
_Tercera decisión_: ¿Cómo visualizo el resultado del _Flow #3_?

