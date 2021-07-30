# omer-rest-api-invoker

The omer-rest-api-invoker library gives a generic implementation of how endpoints can be invoked.
The develop does not need to create objects for Response Entity, Http Entity, and how the actual invocation would happen.
The developer only needs to pass the 
1. end point
2. http method
3. http headers
4. the return type of the exposed endpoint

Over loaded methods have been provided so that the developer does not need to pass null for a parameter which is not required

Passing the URL makes code redundant, so an implementation needs to be provided which can contain the URL, and moving forward only the endpoints need to be passed

With the default interface, two more sub interfaces have been provided
1. Fault Tolerance i.e. Circuit Breaker
2. Retry

The developer can either use of these interfaces if he wants to make sure his endpoint invocations are fault tolerant.

The developer can over ride the default properties of the Circuit Breaker config and retry config by adding the overridden properties to the properties/yml files.

