package com.souki.game.adventure.entity.systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.souki.game.adventure.entity.components.LightComponent;
import com.souki.game.adventure.entity.components.TransformComponent;
import com.souki.game.adventure.entity.components.VelocityComponent;

/**
 * Created by vincent on 07/07/2016.
 */

public class MovementSystem extends EntitySystem {
    private ImmutableArray<Entity> entities;

    private ComponentMapper<TransformComponent> tm = ComponentMapper.getFor(TransformComponent.class);
    private ComponentMapper<VelocityComponent> vm = ComponentMapper.getFor(VelocityComponent.class);
    private ComponentMapper<LightComponent> lm = ComponentMapper.getFor(LightComponent.class);

    public MovementSystem() {
        priority = 1;
    }

    public void addedToEngine(Engine engine) {
        entities = engine.getEntitiesFor(Family.all(TransformComponent.class, VelocityComponent.class).get());
    }

    public void update(float deltaTime) {
        for (int i = 0; i < entities.size(); ++i) {
            Entity entity = entities.get(i);
            TransformComponent transform = tm.get(entity);
            VelocityComponent velocity = vm.get(entity);
            LightComponent light = lm.get(entity);

            transform.position.x += velocity.x * deltaTime;
            transform.position.y += velocity.y * deltaTime;
            if (light != null) {
                light.position.x += velocity.x * deltaTime;
                light.position.y += velocity.y * deltaTime;
            }
        }
    }
}
