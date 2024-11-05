# RockWorksChat

All in one chat plugin for syncing chat across multiple servers, with infinite channels!

Config With Comments
```yaml
# ##########################################
# #             RockWorksChat              #
# #   Stone Works Chat Syncing by Preva1l  #
# ##########################################

channels:
- id: global # The identifier for the channel, not case-sensitive
  format: '%vault_prefix% &f%player%: &7%message%' # the format to send the message with.
  toggle-message: '&fNow using &aGlobal &fchat!' # The message that is shown when toggling to that channel
  permission: default # the permission required to use the channel, default allows all players
  commands: # The commands ex: /global, /global my message
  - global
  - globalchat
  always-view: true # If always view is set to true, the player will not need to be in the channel to view the channel
- id: trade
  format: '&2[TC] &r%player%: %message%'
  toggle-message: '&2Trade chat enabled!'
  permission: default
  commands:
  - trade
  - tradechat
  always-view: true
broker: # Redis settings
  host: localhost
  port: 6397
  password: password
  channel: chatsync:message
```
