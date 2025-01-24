function onTick()
    if isServer then
        local pos = dragon:getPosition()
        local hostiles = world:getHostiles(dragon, pos.x(), pos.y(), pos.z(), 10)

        for _, hostile in ipairs(hostiles) do
            if not hostile:isInWaterOrRain() and not hostile:isFireImmune() and not hostile:isOnFire() then
                hostile:setOnFire(80)
            end
        end
    end
end