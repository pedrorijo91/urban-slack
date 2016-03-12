# urban-slack

Slack integration that allows you to search Urban Dictionary terms from the chat room in a similar way to [Giphy](https://slack.com/apps/A0F827J2C-giphy).

### How to setup

First you will need to get your [mashape key](https://market.mashape.com/community/urban-dictionary) so you can make requests to Urban Dictionary API. After creating an account, check [how to get your keys documentation](http://docs.mashape.com/api-keys).

Next, put the keys on your config file at `conf/application.conf`, or just set as an environment variable `MASHAPE_KEY_URBAN`.

Now, simply click on the "Deploy to Heroku" button under this section, or use the command `sbt run`.

[![Deploy](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)

Create a new [slash command](https://slack.com/apps/A0F82E8CA-slash-commands) for your slack team.

Choose the command name you want (the `/command` you will need to type to call the integration) and put your app URL on the configuration with the `/urban` path (eg.: `http://my-urban-integration.herokuapp.com/urban`).

You can provide more configurations to help other users:

* Customize Name (eg. `urban-dictionary`)
* Customize Icon
* Autocomplete help text - Description (eg. `urban dictionary API`)
* Autocomplete help text - Usage Hint (eg. `/urban 'term'`)
* Descriptive Label (eg. `urban dictionary `)


### Couldn't you create a install slack app button?

I was planning to create a Slack app that could be installed from the [Slack App Directory](https://slack.com/apps), but it has several requirements, and in the meanwhile I found that it already exists. [Check it out](https://urban-slack.herokuapp.com)
