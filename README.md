# hystrix-dashboard-bluemix


Sample code for running Netflix Hystrix dashboard on Bluemix Cloud Foundry.
The default dashboard implementation by Netflix uses SSE (Server-Sent Events) protocol to stream Hystric metrics to the dashbard UI which runs in the browser.  SSE does not work with Bluemix, instead the supported protocol for long running connections in Bluemix is web sockets. The sample code provided uses web sockets instead of SSE to communicate with Hystrix dashboard in the browser.  
To allow for metrics aggregation across multiple application instances, standard Turbine AMQP support in Spring Boot for Netflix is used by metrics producer to push all metrics thru same AMQP topic.  You will need to provision an instance of RabbitMQ service in Bluemix and provide connection credentials in application.yml for metrics producer.


To run:
1. 

https://github.com/bijukunjummen/sample-spring-turbine-amqp/tree/master/sample-hystrix-app
