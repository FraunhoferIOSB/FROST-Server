# FROST-Server Helm chart

The [FROST-Server](https://github.com/FraunhoferIOSB/FROST-Server) (**FR**aunhofer **O**pensource **S**ensor**T**hings-Server) is the first complete, open-source implementation of the [OGC SensorThings API Part 1 (Sensing)](http://docs.opengeospatial.org/is/15-078r6/15-078r6.html).

## TL;DR

Declare the Helm repo or update it

    $ helm repo add storeconnect https://storeconnect.github.io/helm-charts
    $ helm repo update storeconnect
    
Install the FROST-Server chart

    $ helm install storeconnect/frost-server  

## Introduction

This chart bootstraps a [FROST-Server](https://github.com/FraunhoferIOSB/FROST-Server) deployment on a [Kubernetes](https://kubernetes.io/) cluster using the [Helm](https://helm.sh/) package manager.

## Prerequisites

- Have a [Kubernetes](https://kubernetes.io/) 1.4+ cluster. If you do not already have a cluster, you can:
    - Create one by using [Minikube](https://kubernetes.io/docs/getting-started-guides/minikube)
    - Or use [Katacoda](https://www.katacoda.com/courses/kubernetes/playground)
    - Or use [Play with Kubernetes](http://labs.play-with-k8s.com/)
- Have the `kubectl` command-line tool correctly configured to communicate with your Kubernetes cluster
- Have the [`helm`](https://helm.sh/) command-line tool [correctly initialized with your Kubernetes cluster](https://docs.helm.sh/using_helm/#quickstart-guide)
- (Optionally but recommended) Have a [Ingress controller](https://kubernetes.io/docs/concepts/services-networking/ingress/) installed on your Kubernetes cluster. Need to have [Beta APIs](https://kubernetes.io/docs/reference/using-api/api-overview/) enabled.

## Installing the Chart

Before to go, declare the Helm repo or update it
    
    $ helm repo add storeconnect https://storeconnect.github.io/helm-charts
    $ helm repo update storeconnect
    
Then, to install the chart with the [release name](https://docs.helm.sh/using_helm/#quickstart-guide) `my-release`

    $ helm install --name my-release storeconnect/frost-server  
    
This command deploys FROST-Server on the Kubernetes cluster in the default configuration. The [configuration](#configuration) section lists the parameters that can be configured during installation.

By default, the FROST-Server instance is reachable at the `http://frost-server:30080` URL (concatenation of the `frost.http.serviceHost` and `frost.http.ports.http.servicePort` configuration values).

> **Warning**: Make sure to be able to resolve the `frost-server` DNS name by adding a rule either in your DNS server or in your local DNS resolver (e.g. `/etc/hosts` in Unix-based environments), or use an IP instead of a DNS name by setting the `frost.http.serviceHost` value. 

### Deployed FROST-Server resources

This chart deploys a fully operational FROST-Server stack composed of:
- A (or several, depending on the number of replicas) FROST-Server's HTTP service(s)
- A (or several, depending on the number of replicas) FROST-Server's MQTT service(s)
    - associated to an internal MQTT broker ([Eclipse Mosquitto](https://projects.eclipse.org/projects/technology.mosquitto))
- (Not enabled by default) An internal FROST-Server's database
    - associated to a local volume (disabled by default but can be enabled as explained [here](#about-persistence-support))

To have a view about the deployed FROST-Server resources in the `my-release` deployment execute:

    $ helm status my-release
    
To visualize logs about deployed Helm release's pods, execute:

    $ kubeclt logs -l release=my-release
    
Or, more precisely: 

    $ kubeclt get pods -l release=my-release
    $ kubectl logs <pod name>
    
Where `<pod name>` is your desired pod name

Or, even simpler, by using [kubetail](https://github.com/johanhaleby/kubetail):

    $ kubetail -l release=my-release

## Uninstalling the Chart

To uninstall/delete the `my-release` release:

    $ helm delete my-release

This command removes all the Kubernetes components associated with the chart and deletes the release.

## Configuration
    
The following table lists the configurable parameters of the FROST-Server chart and their default values.

Parameter                                   | Description                                                                                                                                                                                                                                           | Default                                                                                   
------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------- 
`name`                                      | Override of the base name for any FROST-Server Kubernetes component                                                                                                                                                                                   | `nil` (use the chart name, `frost-server`, by default)
`frost.http.replicas`                       | Number of FROST-Server HTTP module replicas                                                                                                                                                                                                           | `1`
`frost.http.ports.http.nodePort`            | The external port (node port) of the FROST-Server HTTP service, **if not using Ingress**                                                                                                                                                              | `30080`
`frost.http.ports.http.servicePort`         | The internal port of the FROST-Server HTTP module                                                                                                                                                                                                     | `80`
`frost.http.ingress.enabled`                | If Ingress needs to be enabled for the FROST-Server HTTP module. See [bellow](#ingress) for more information                                                                                                                                          | `false`
`frost.http.serviceHost`                    | The host used by the [`serviceRootURL`](https://github.com/FraunhoferIOSB/FROST-Server/blob/master/docs/settings.adoc#general-settings) mandatory FROST-Server configuration parameter                                                                | `frost-server`
`frost.http.defaultCount`                   | The default value for the $count query option used by the FROST-Server HTTP module                                                                                                                                                                    | `false`
`frost.http.defaultTop`                     | The default value for the $top query option used by the FROST-Server HTTP module                                                                                                                                                                      | `100`
`frost.http.maxTop`                         | The maximum allowed value for the $top query option used by the FROST-Server HTTP module                                                                                                                                                              | `1000`
`frost.http.useAbsoluteNavigationLinks`     | If `true`, FROST-Server HTTP's `navigationLinks` are absolute, otherwise relative                                                                                                                                                                     | `true`
`frost.http.db.alwaysOrderbyId`             | Always add an `orderby=id asc` to FROST-Server HTTP's database queries to ensure consistent paging                                                                                                                                                    | `false`
`frost.http.db.maximumConnection`           | The maximum number of database connections used by the FROST-Server HTTP module                                                                                                                                                                       | `10`
`frost.http.db.maximumIdleConnection`       | The maximum number of idle database connections to keep open by the FROST-Server HTTP module                                                                                                                                                          | `10`
`frost.http.db.minimumIdleConnection`       | The minimum number of idle database connections to keep open by the FROST-Server HTTP module                                                                                                                                                          | `10`
`frost.http.bus.sendWorkerPoolSize`         | The number of FROST-Server HTTP worker threads to handle sending messages to the bus                                                                                                                                                                  | `10`
`frost.http.bus.sendQueueSize`              | The size of the FROST-Server HTTP message queue to buffer messages to be sent to the bus                                                                                                                                                              | `100`
`frost.http.bus.recvWorkerPoolSize`         | The number of FROST-Server HTTP worker threads to handle messages coming from the bus                                                                                                                                                                 | `10`
`frost.http.bus.maxInFlight`                | The maximum number of FROST-Server HTTP _in-flight_ messages to allow on the MQTT bus                                                                                                                                                                 | `50`
`frost.db.ports.postgresql.servicePort`     | The internal port of the FROST-Server database service                                                                                                                                                                                                | `5432`
`frost.db.persistence.enabled`              | If data persistence needs to be enabled. See [bellow](#persistence) for more information                                                                                                                                                              | `false`
`frost.db.persistence.existingClaim`        | If set, then use an existing [PersistenceVolumeClaim](https://kubernetes.io/docs/concepts/storage/persistent-volumes/#lifecycle-of-a-volume-and-claim) for the FROST-Server database volume. See [bellow](#persistence) for more information          | `nil` (use the builtin PersistenceVol
`frost.db.persistence.storageClassName`     | The [StorageClassName](https://kubernetes.io/docs/concepts/storage/persistent-volumes/#class) to use by the FROST-Server database persistence. See [bellow](#persistence) for more information                                                        | `nil` (use the default StorageClass currently in use)
`frost.db.persistence.accessModes`          | List of [AccessModes](https://kubernetes.io/docs/concepts/storage/persistent-volumes/#access-modes) to claim if FROST-Server database persistence is enabled. See [bellow](#persistence) for more information                                         | `{ReadWriteOnce}`
`frost.db.persistence.capacity`             | The storage capacity required by the FROST-Server database persistence                                                                                                                                                                                | `10Gi`
`frost.db.persistence.local.nodeMountPath`  | The mount path to use if using the `local` StorageClassName as FROST-Server database StorageClass persistence. See [bellow](#persistence) for more information                                                                                        | `/mnt/frost-server-db`
`frost.db.database`                         | The FROST-Server database name to use                                                                                                                                                                                                                 | `sensorthings`
`frost.db.username`                         | The _base64_ username to use when connecting to the FROST-Server database                                                                                                                                                                             | `c2Vuc29ydGhpbmdz` (`sensorthings`)
`frost.db.password`                         | The _base64_ password to use when connecting to the FROST-Server database                                                                                                                                                                             | `bm93eW91Y2FuY2hhbmdlaXQ=` (`nowyoucanchangeit`)
`frost.db.idGenerationMode`                 | Determines how entity ids are generated by any FROST-Server module. See [here](https://github.com/FraunhoferIOSB/FROST-Server/blob/master/docs/settings.adoc#persistence-settings) for more information                                               | `ServerGeneratedOnly`
`frost.db.implementationClass`              | The Java class used for persistence by any FROST-Server module                                                                                                                                                                                        | `[...].PostgresPersistenceManagerLong` (see [1] bellow for the complete value) 
`frost.mqtt.enabled`                        | If MQTT support needs to be enabled. See [bellow](#mqtt) for more information                                                                                                                                                                         | `true`
`frost.mqtt.replicas`                       | The number of FROST-Server MQTT module replicas                                                                                                                                                                                                       | `1`
`frost.mqtt.ports.mqtt.nodePort`            | The external port (node port) of the FROST-Server MQTT service                                                                                                                                                                                        | `30883`
`frost.mqtt.ports.mqtt.servicePort`         | The internal port of the FROST-Server MQTT service                                                                                                                                                                                                    | `1883`
`frost.mqtt.ports.websocket.nodePort`       | The external port (node port) of the FROST-Server MQTT websocket service                                                                                                                                                                              | `30876`
`frost.mqtt.ports.websocket.servicePort`    | The internal port of the FROST-Server MQTT websocket service                                                                                                                                                                                          | `9876`
`frost.mqtt.stickySessionTimeout`           | Timeout (in seconds) of sticky time sessions used by the FROST-Server MQTT server                                                                                                                                                                     | `10800` (3 hours)
`frost.mqtt.qos`                            | Quality of Service Level for MQTT messages                                                                                                                                                                                                            | `2`
`frost.mqtt.subscribeMessageQueueSize`      | Queue size for messages to be published via MQTT                                                                                                                                                                                                      | `100`
`frost.mqtt.subscribeThreadPoolSize`        | Number of threads use to dispatch MQTT notifications                                                                                                                                                                                                  | `10`
`frost.mqtt.createMessageQueueSize`         | Queue size for create observation requests via MQTT                                                                                                                                                                                                   | `100`
`frost.mqtt.createThreadPoolSize`           | Number of threads use to dispatch observation creation requests                                                                                                                                                                                       | `10`
`frost.mqtt.maxInFlight`                    | The maximum number of _in-flight_ messages to allow when sending notifications                                                                                                                                                                        | `50`
`frost.mqtt.db.alwaysOrderbyId`             | Always add an `orderby=id asc` to to FROST-Server MQTT's database queries to ensure consistent paging                                                                                                                                                 | `false`
`frost.mqtt.db.maximumConnection`           | The maximum number of database connections to use by the FROST-Server MQTT module                                                                                                                                                                     | `10`
`frost.mqtt.db.maximumIdleConnection`       | The maximum number of idle database connections to keep open by the FROST-Server MQTT module                                                                                                                                                          | `10`
`frost.mqtt.db.minimumIdleConnection`       | The minimum number of idle database connections to keep open by the FROST-Server MQTT module                                                                                                                                                          | `10`
`frost.mqtt.bus.sendWorkerPoolSize`         | The number of FROST-Server MQTT worker threads to handle sending messages to the MQTT bus                                                                                                                                                             | `10`
`frost.mqtt.bus.sendQueueSize`              | The size of the FROST-Server MQTT message queue to buffer messages to be sent to the MQTT bus                                                                                                                                                         | `100`
`frost.mqtt.bus.recvWorkerPoolSize`         | The number of FROST-Server MQTT worker threads to handle messages coming from the MQTT bus                                                                                                                                                            | `10`
`frost.mqtt.bus.maxInFlight`                | The maximum number of FROST-Server MQTT _in-flight_ messages to allow on the MQTT bus                                                                                                                                                                 | `50`
`frost.bus.ports.bus.servicePort`           | The internal port of the FROST-Server Messages Bus service                                                                                                                                                                                            | `1883`
`frost.bus.implementationClass`             | The Java class that is used to connect to the message bus, common for any FROST-Server modules                                                                                                                                                        | `[...].MqttMessageBus` (see [2] bellow for the complete value)
`frost.bus.topicName`                       | The MQTT topic to use as a message bus by any FROST-Server module                                                                                                                                                                                     | `FROST-Bus`
`frost.bus.qosLevel`                        | The Quality of Service Level for the MQTT bus by any FROST-Server module                                                                                                                                                                              | `2`

> [1] The complete default `frost.db.implementationClass` value is `de.fraunhofer.iosb.ilt.sta.persistence.postgres.longid.PostgresPersistenceManagerLong`.

> [2] The complete default `frost.bus.implementationClass` value is `de.fraunhofer.iosb.ilt.sta.messagebus.MqttMessageBus`.

Specify each parameter using the `--set key=value[,key=value]` argument to `helm` `install|upgrade`. For example,

    $ helm install --name my-release \
        --set key_1=value_1,key_2=value_2 \
        storeconnect/frost-server
        
Alternatively, a YAML file that specifies the values for the parameters can be provided while installing the chart. For example,

    # example for staging
    $ helm install --name my-release -f values.yaml storeconnect/frost-server  

> **Tip**: You can use the default [values.yaml](./values.yaml)

More information about the FROST-Server configuration can be found [here](https://github.com/FraunhoferIOSB/FROST-Server/blob/master/docs/settings.adoc). 

## MQTT

As described in the [OGC SensorThings API specification](http://docs.opengeospatial.org/is/15-078r6/15-078r6.html#85), MQTT support is an optional extension but enabled by default in the FROST-Server Helm chart.
To disable MQTT support, override the `frost.mqtt.enabled` configuration value to `false`. 

    $ helm install --set frost.mqtt.enabled=false storeconnect/frost-server

## Persistence

By default, the FROST-Server database is working without data persistence. Thus, if the Helm release or the FROST-Server database pod is deleted, then all saved data is lost.
To enable data persistence, turn on the `frost.db.persistence.enabled` configuration parameter:

    $ helm install --set frost.db.persistence.enabled=true storeconnect/frost-server

Once FROST-Server database persistence is enabled, then the FROST-Server will either:
- use its own [PersistenceVolumeClaim](https://kubernetes.io/docs/concepts/storage/persistent-volumes/#lifecycle-of-a-volume-and-claim), which is described bellow
- or use an existing PersistenceVolumeClaim, if the `frost.db.persistence.existingClaim` is set

If persistence is enabled and no existing PersistenceVolumeClaim is defined (`frost.db.persistence.existingClaim` is unset), then the FROST-Server chart will claim a [PersistentVolume](https://kubernetes.io/docs/concepts/storage/persistent-volumes/) that fits with its associated [StorageClass](https://kubernetes.io/docs/concepts/storage/storage-classes/) name.
By default, no name is defined, then the default StorageClass currently in use in the Kubernetes cluster will be used.
But you can override this behaviour by setting the `frost.db.persistence.storageClassName` configuration value with your desired StorageClass name to use. 

If necessary, the FROST-Server chart also defines its own StorageClass name, `frost-server-db-local`, bound to a [builtin local volume](./templates/db-local-volume.yaml), which stores data in a local directory within the cluster (more precisely within the node where this local volume is deployed).
To enable it, set the `frost.db.persistence.storageClassName` to `frost-server-db-local` and precise the folder where data need to be persisted on the node

    $ helm install \
        --set frost.db.persistence.enabled=true,frost.db.persistence.storageClassName=frost-server-db-local,frost.db.persistence.local.nodeMountPath=/mnt/frost-server-db \
        storeconnect/frost-server

> **Warning #1**: The `local` StorageClass cannot be scaled.

> **Warning #2**: The `local` StorageClass can only be used if only the ReadWriteOnce [AccessMode](https://kubernetes.io/docs/concepts/storage/persistent-volumes/#access-modes) is claimed (check the `frost.db.persistence.accessModes` configuration parameter).

## Ingress

The FROST-Server HTTP component can be accessed through an [Ingress controller](https://kubernetes.io/docs/concepts/services-networking/ingress/). By default, Ingress is disabled but can be enabled thanks to the `frost.http.ingress.enabled` option:

    $ helm install --set frost.http.ingress.enabled=true storeconnect/frost-server
    
Or if you want to enable it in your current living `my-release` release:
    
    $ helm upgrade --set frost.http.ingress.enabled=true my-release storeconnect/frost-server
    
Once Ingress is enabled on the FROST-Server HTTP component, then the FROST-Server HTTP API can be accessed at `http://<frost.http.serviceHost>` (`http://frost-server` by default), on the standard 80 HTTP port, without being constrained to specify the `frost.http.ports.http.nodePort` port.

 > **Warning**: `frost.http.serviceHost` needs to be a DNS name. Make sure to be able to resolve it by adding a rule either in your DNS server or in your local DNS resolver (e.g. `/etc/hosts` in Unix-based environments).