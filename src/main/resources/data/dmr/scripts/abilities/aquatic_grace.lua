function onTick()
    if hasOwner and isServer and dragon:isInWater() then
        owner:addEffect("night_vision", 20*20, 0)
    end
end