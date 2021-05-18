---
layout: default
title: Updating &amp; Deleting Entities
category: deploymentTutorial
topCategory: STA
order: 3
---

# Updating &amp; Deleting Entities

The OGC SensorThings API is a full REST API that not only covers creating and requesting entities, but also updating and deleting them.


## Updating Entities

Entities can be updated by sending a HTTP PATCH request to the selfLink of an entity, with in the body only the fields that need to be changed.
The following request updates only the name of Thing 999.

```
PATCH https://example.org/FROST-Server/v1.1/Things(999)
```
```javascript
{
  "name" : "My Updated Kitchen"
}
```

In the FROST-Server HTTP Tool, select `PATCH` in the dropdown, put a selfLink in the URL box, add the updated JSON in the data field and click `Execute`.


## Deleting Entities

Deleting entities is done by sending a HTTP DELETE request to the selfLink of an entity.
The following request deletes Thing 999.

```
DELETE https://example.org/FROST-Server/v1.1/Things(999)
```

In the FROST-Server HTTP Tool, select `DELETE` in the dropdown, put a selfLink in the URL box and click `Execute`.

Beware though, deleting an entity also deletes all entities that depend on this entity.
That means when deleting a Datastream, all Observations in that Datastream are also deleted.
When deleting a Thing, all Datastreams of that Thing are deleted, and with those all their Observations.

