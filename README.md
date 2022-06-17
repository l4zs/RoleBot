# RoleBot

l4zs' cool self-hosted role-bot.

## Features

- create multiple role messages whereever you want
- for each message you can configure the following:
    - title
    - description
    - a list of roles, for which you can configure the following:
        - label
        - description
        - custom emoji

![img.png](assets/img.png)

## Deployment

###### Example Environment file

<details>
<summary>.env</summary>

```properties
ENVIRONMENT=PRODUCTION
DISCORD_TOKEN=<discord-token>
MONGO_URL=mongodb://bot:bot@mongo
MONGO_DATABASE=bot_prod
LOG_LEVEL=DEBUG
BOT_OWNERS=<owner-id>
OWNER_GUILD=<owner-guild-id>
```

</details>
