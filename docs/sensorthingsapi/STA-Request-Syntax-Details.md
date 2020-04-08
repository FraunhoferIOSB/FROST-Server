---
layout: default
title: Request Syntax Details
category: STA
order: 4
---

# Mixing Request Parameters

Top-level `$options` are separated with a `&`. 

    v1.1/Things?$expand=Locations&$top=1

Use `,` to separate items in a `$select` or `$expand`.

    v1.1/Things?$expand=Locations,Datastreams&$top=1

Within a `$expand`, you use `;` to separate `$options`.

    v1.1/Things?$expand=Locations($select=id,name;$top=1),Datastreams($select=name,id;$top=1)&$top=1
