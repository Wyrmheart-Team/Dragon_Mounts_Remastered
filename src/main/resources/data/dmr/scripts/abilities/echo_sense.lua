function onTick()
    if isServer then
        local pos = dragon:getPosition()
        local hostiles = world:getHostilesInRange(owner, pos.x, pos.y, pos.z, 20)

        for hostile in hostiles do
            hostile:addEffect("minecraft:glowing", 80)
        end
    end
end