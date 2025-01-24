function onTick()
    if isServer then
        local pos = dragon:getPosition()
        local hostiles = world:getHostiles(dragon, pos.x(), pos.y(), pos.z(), 20)

        for _, hostile in ipairs(hostiles) do
            hostile:addEffect("minecraft:glowing", 80)
        end
    end
end