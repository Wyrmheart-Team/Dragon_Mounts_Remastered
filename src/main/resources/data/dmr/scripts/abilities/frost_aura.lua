function onTick()
    if isServer then
        local pos = dragon:getPosition()
        local hostiles = world:getHostiles(dragon, pos.x(), pos.y(), pos.z(), 10)

        for _, hostile in ipairs(hostiles) do
            hostile:addEffect("minecraft:slowness", 100, 1)
        end
    end
end