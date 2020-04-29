---
layout: default
title: Custom Entity Linking
category: extensions
order: 3
---

# Custom Entity Linking

In many use-cases the existing links between entity types that the SensorThings API offers are not sufficient to describe the data.
A generic way to link between various objects, internal and external is needed.
Since the SensorThings API v1.x does not have special fields for links, the links will have to go in the properties field that each entity type has.

https://github.com/INSIDE-information-systems/SensorThingsAPI/issues/7


## Internal links

To be functional, internal links need:
- The entity id of the target entity
- The entity type of the target entity
- The name of the link

Furthermore, they need to be formatted in a way that the server can easily detect the links.
This makes it possible for the server to generate absolute URLs for these links, and allow searching and expanding of the links.

The SensorThings API uses a special @ notation for internal properties:
- Entity ids in STA are labelled @iot.id
- Navigation links are <TargetEntityType>@iot.navigationLink
- Counts for navigation links are <TargetEntityType>@iot.count
- Nextlinks are @iot.nextLink

We could introduce something similar. When linking to another Entity, add an entry to the properties map like:

    "<linkName>.<TargetEntityType>@iot.id": <Target Entity Id>
    "building.Thing@iot.id": 45
    "sensorType.Sensor@iot.id": 16
    "aggregateFor.Datastream@iot.id": "123e4567-e89b-12d3-a456-426655440000"


### NavigationLinks

The server can operate on these, for instance by adding a navigationLink when returning the properties:

    Get V1.0/Things(1)
    {
        "Properties" : {
            "building.Thing@Iot.Id": 45,
            "building.Thing@Iot.Navigationlink": "http://example.org/Frost-Server/V1.0/Things(45)"
        }
    }

When updating or creating entities, if there is a property of the type `<linkName>.<TargetEntityType>@iot.id` then the property `<linkName>.<TargetEntityType>` and all other properties starting with `<linkName>.<TargetEntityType>@` will be removed by the server before storing the properties.

Custom entity links to not have to be placed at the root of the properties map, though a server implementation may limit how deep these links may appear.


### Expand support

The server can support $expand on these entity links, by including the full target entity next to the link:

    GET v1.0/Things(1)?$expand=properties/building.Thing
    {
        "properties" : {
            "building.Thing@iot.id": 45,
            "building.Thing": { <expanded Thing> }
        }
    }

As mentioned above, when updating or creating an entity, these server-generated entries will be ignored/removed.


### Filter supprt

The server can support $filter on these entity links just like on normal navigation links:

    GET v1.0/Things(1)?$filter=properties/building.Thing/name eq 'Building 1'


### Registering & announcing links

The behaviour described above can be exposed by a server without the server knowing in advance which links exist.
When formatting the results of a request, the server can iterate through the properties, and generate any required navigationLinks and expands.
To enable efficient filtering on these properties, the server will need to know which links (may) exist before fetching data from the database.
Pre-registering the existing links on the server will also allow the server to announce the existence of those links, and their semantics, to clients.
It also makes it possible for the server to generate back-links from the entities that are linked to, but the details for that still need to be specified.

Pre-registered links are announced in the `serverSettings` part of the server root document.

    {
      "serverSettings": {
        "conformance": [
          "<our register requirement class uri>"
        ],
        "<our main requirement class uri>": {
          "registeredLinks": {
            "<sourceType>/properties/<linkName>": {
              "targetType": "<targetType>",
              "description": "A human readable description of the link"
            }
          }
        }
      }
    }

By specifying the full path, links do not have to be top-level entries in the properties object, but can be nested deeper.
For example:

    "Thing/properties/links/building": {
      "targetType": "Thing",
      "description": "The building a room is part of."
    }


## Open issues

Some things are not specified yet and are in need of discussion:

- Cardinality many-to-many: linking from one item to many items, using the same name, instead of just to one item.
- Back-linking: how to generate and annouce links that are two-way instead of just one-way.


## Conformance Class

If a server implements this specification fully, it must register the following URL in the SensorThings (v1.1 and up) index document:

    https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/EntityLinking/Linking.md

If the server only implements parts, it may register any of the following URLs:

    https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/EntityLinking/Linking.md#NavigationLinks
    https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/EntityLinking/Linking.md#Expand
    https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/EntityLinking/Linking.md#Filter
    https://github.com/INSIDE-information-systems/SensorThingsAPI/blob/master/EntityLinking/Linking.md#Register


