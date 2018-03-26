# SoukiAdventure
## Interactions
```JSON
  {
    // interaction id, must be the same in the tile file
    "id": "cart1", 
    
    // the json template file
    "template": "obstacle_mines_cart.json", 
    
    // list of properties, it depends to the interaction, they overwrite properties defined in template
    "properties": {  
      // generic property
      "startState":"IDLE" 
     
      // interaction dependant properties
      "pathId": "path1",
      "autoStart": "true",
      "looping": "true"
    },
    
    // list of actions triggered on events
    "eventsAction": [
      {
        "action": {
          // action types : DIALOG,
        SET_STATE,
        WAKEUP,
        OPEN,
        CLOSE,
        REMOVED,
        LAUNCH_EFFECT,
        ACTIVATE_QUEST,
        SET_PATH;
        
          "type": "SET_PATH",
          "value": "path2"
        },
        "inputEvents": [
          {
            "sourceId": "activator1",
            "type": "STATE",
            "value": "ACTIVATED",
            "isPersistent":"true"
          },
          {
            "sourceId": "THIS",
            "type": "END_PATH",
            "value": "path1",
            "isVolatile":"true"
          }

        ]
      }
    ],
    "outputEvents": []
  }
```
