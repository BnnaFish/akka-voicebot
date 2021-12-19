# Description

Proof-of-concept project.

The idea is to use actor system to handle Q&A conversation with user.

Each users reply persists in DB in append-only fashion. That's how system supports fault tolerance.

Another feature is the opportunity to count some metrics like conversion, mean, top-k, etc.
It's should be easy to add new stats metric with ability to recalculate it backward in time.

Although script engine should have ability to take into account internal logic or external API to answer differently. Meaning it not just hardcoded mapping.

# How-to

Up DB first via:
``` console
docker-compose up postgres
```

Rollup migrates:
```console
sbt flywayMigrate
```

Up web server:
```console
sbt voicebot/run
```

Make request:
```console
curl --location --request POST 'http://127.0.0.1:8080/api/v2/what-to-reply' \
--header 'Content-Type: application/json' \
--data-raw '{
    "sessionId": 1,
    "userId": 2,
    "recognizedText": "Hi"
}'

{"textToReply":"Do you what to buy a house?"}
```

# Dialog examples

##### Successed

> \- Hi

> \- Do you what to buy a house?

> \- Ya

> \- What amount of initial sum do you allready have?

> \- 1000000

> \- What amount of mortgage do you need?

> \- 2000000

> \- Mortgage is approved. Congratulations!

##### No enought money branch

> \- Hi

> \- Do you what to buy a house?

> \- Yes

> \- What amount of initial sum do you allready have?

> \- 1000000

> \- What amount of mortgage do you need?

> \- 10000000

> \- Sorry, but not enough initial sum.

##### Unrecognized intent

> \- Hi

> \- Do you what to buy a house?

> \- Yes

> \- What amount of initial sum do you allready have?

> \- foobar

> \- Something goes wrong. Оperator will call you back soon.
