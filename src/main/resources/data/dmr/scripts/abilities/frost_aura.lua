function onTick()
    if isServer then
        local pos = dragon:getPosition()
        local hostiles = world:getHostilesInRange(owner, pos.x, pos.y, pos.z, 10)

        for hostile in hostiles do
            hostile:addEffect("minecraft:slowness", 100, 1)
        end
    end
end