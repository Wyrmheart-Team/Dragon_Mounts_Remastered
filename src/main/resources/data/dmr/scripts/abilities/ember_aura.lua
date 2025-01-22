function onTick()
    if isServer then
        local pos = dragon:getPosition()
        local hostiles = world:getHostilesInRange(owner, pos.x, pos.y, pos.z, 10)

        for hostile in hostiles do
            if not hostile:isInWaterOrRain() and not hostile:isFireImmune() and not hostile:isOnFire() then
                hostile:setOnFire(4)
            end
        end
    end
end