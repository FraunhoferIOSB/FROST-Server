# FormatArray design


Query with explicit selection of complex property parts
'''
/Datastreams?
    $select=id,name,phenomenonTime/start,phenomenonTime/end&
    $count=true&
    $expand=
        ProximateFeatureOfIterest($select=id,name,properties),
        ObservedProperties($select=id,name;$count=true),
        Observations($select=id,phenomenonTime,result;$count=true)&
    $format=jsonarray
'''


Result, order can deviate from order of query, can be parsed without knowledge of the query.
Type and structure information must precede the values, to allow streaming processing of the data.
'''json
{
    "@context": "...",
    "structure": [
        "id",
        "name",
        {                              /* Single entity ProximateFeatureOfIterest, properties is an open type */
            "property": "ProximateFeatureOfIterest",
            "structure": [
                "id",
                "name",
                "properties"
            ]
        },
        {                              /* contents of EntitySet ObservedProperties, array of arrays */
            "property": "ObservedProperties",
            "structure": [
                "id",
                "name"
            ]
        },
        "ObservedProperties@count",                     /* Count for EntitySet, if requested */
        "ObservedProperties@nextLink",                  /* NextLink for EntitySet, may be null */
        {
            "property": "Observations",
            "structure": [                              /* contents of EntitySet Observations */
                "id",
                {
                    "property": "phenomenonTime",   /* phenomenonTime is complex */
                    "structure": [
                        "start",
                        "end"
                    ]
                },
                "result"
            ]
        }
        "Observations@count",                           /* Count for EntitySet */
        "Observations@nextLink",                        /* NextLink for EntitySet */
    ],
    "value": [
        [
            1,
            "Datastream 1",
            "2026-01-01T00:00:00Z",    /* Because phenomenonTime/start is explicitly selected, it is extracted from the complex property */
            "2028-01-01T00:00:00Z",    /* Because phenomenonTime/end is explicitly selected, it is extracted from the complex property */
            [
                1,
                "Sampling point 1",
                { "property1": "value 1"}
            ],
            1,
            null,
            [
                [1, ["2026-01-01T00:00:00Z", "2026-01-01T01:00:00Z"], 42],
                [2, ["2026-01-01T01:00:00Z", "2026-01-01T02:00:00Z"], 42],
                [3, ["2026-01-01T02:00:00Z", "2026-01-01T03:00:00Z"], 42],
                [4, ["2026-01-01T03:00:00Z", "2026-01-01T04:00:00Z"], 42],
            ],
            4200
            "<nextLink for Observations>"
        ],
        [
            2,
            "Datastream 2",
            "2026-01-01T00:00:00Z",    /* Because phenomenonTime/start is explicitly selected, it is extracted from the complex property */
            "2028-01-01T00:00:00Z",    /* Because phenomenonTime/end is explicitly selected, it is extracted from the complex property */
            [
                2,
                "Sampling point 2",
                { "property1": "value 2"}
            ],
            1,
            null,
            [
                [1, ["2026-01-01T00:00:00Z", "2026-01-01T01:00:00Z"], 42],
                [2, ["2026-01-01T01:00:00Z", "2026-01-01T02:00:00Z"], 42],
                [3, ["2026-01-01T02:00:00Z", "2026-01-01T03:00:00Z"], 42],
                [4, ["2026-01-01T03:00:00Z", "2026-01-01T04:00:00Z"], 42],
            ],
            4200
            "<nextLink for Observations>"
        ],
    ],
    "@nextLink": "<nextLink for Datastreams>",
    "@count": 42
}

'''

