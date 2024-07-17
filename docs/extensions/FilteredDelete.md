---
layout: default
title: Filtered Delete
category: extensions
order: 19
---

# Filtered Delete

Normally entities can only be deleted individually, one at a time.
When performing maintenance, like deleting all Observations older than a year, this is often too slow.
This extension allows the deleting of many entities, by sending a DELETE to an EntitySet, with a `$filter`.

## Conformance Class

The conformance class this extension must register in the SensorThings (v1.1 and up) index document is:

    https://fraunhoferiosb.github.io/FROST-Server/extensions/FilteredDelete.html


