---
layout: default
title: Importing Observations
category: deploymentTutorial
topCategory: STA
order: 2
---

# Importing Observations

Creating Observations in the SensorThings API is no different than creating other entities.
Posting a simple JSON object to `v1.1/Observations` or `v1.1/Datastreams(...)/Observations` is enough.
This means it is trivial to write a script that parses a CSV file, turns each row into an Observation,
and Posts it to a server.

Here is just such a page: [2_Importer.html](2_Importer.html).

## Inner workings

The page itself is very simple. It contains:
- A label for the URL field
- A text input for the URL
- A button to start the process, with an `onclick` that calls some Javascript function
- A textarea that the CSV goes into
- A placeholder for error or success messages

```html
<div class="flex-container">
    <label for="url" style="padding: 5px">URL:</label>
    <input type="text" id="url" name="url" style="flex-grow: 1" value="http://localhost:8080/FROST-Server/v1.1/Datastreams(999)/Observations">
    <input type="button" value="execute" onclick="execute();">
</div>
<div>
    <textarea id="data" name="content" rows="10" cols="80">
2020-11-19T00:00:00.000Z,1
...
2020-11-19T23:00:00.000Z,4
    </textarea>
</div>
<div id="result"></div>
```

In the header is the Javascript that does the processing.

The first method gathers data and calls the CSV parsing method:
```javascript
function execute() {
    let url = document.getElementById('url').value;
    let data = document.getElementById('data').value;
    document.getElementById('result').innerHTML = '<p>Importing to ' + url + '</p>';
    processData(url, data);
}
```

The second method parses the CSV. It:
- splits the text into lines
- splits each line into parts
- ensures there are two fields on the line
- creates an Object, with
  - the contents of the first field in the property `phenomenonTime'
  - the contents of the second field in the property `result'
- sends the object and url to the next method

```javascript
function processData(url, data) {
    var allLines = data.split(/\r\n|\n/);
    var lines = [];

    for (var i=0; i<allLines.length; i++) {
        var data = allLines[i].split(',');
        if (data.length === 2) {
            let observation = {
                phenomenonTime: data[0].trim(),
                result: data[1].trim()
            };
            post(url, JSON.stringify(observation));
        }
    }
}
```

The last method posts the data to the given url, and adds the result of the post to the output field.

```javascript
function post(url, data) {
    var request = new XMLHttpRequest();
    request.addEventListener("load", function (e) {
        if (request.readyState === 4) {
            let p = document.createElement('p');
            if (request.status >= 200 && request.status < 300) {
                let location = request.getResponseHeader('Location');
                p.innerText = 'Done: ' + location;
            } else {
                p.innerText = 'Error ' + request.status + ": " + request.responseText + "";
            }
            document.getElementById('result').appendChild(p);
        }
    });
    request.addEventListener("error", function (e) {
        let p = document.createElement('p');
        p.innerText = 'Error: ' + request.statusText;
        document.getElementById('result').appendChild(p);
    });
    request.open('POST', url, true);
    request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    request.send(data);
}
```
That's it, a simple CSV importer.


## More complicated importing

The simple importer shown above has a lot of shortcomings of course.
 The main one is that it will simply import all data every time the button is clicked, regardless of the data already being there.







