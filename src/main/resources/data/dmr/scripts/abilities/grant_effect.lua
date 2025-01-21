function onTick()
    if hasOwner and isServer then
        local effect = params["effect"]
        local duration = params["duration"] or 20
        local amplifier = params["amplifier"] or 0
        local range = params["range"] or 5
        local grantToDragon = params["affects_dragon"] or false

        if dragon:getDistance(owner) <= range or dragon:hasPassenger(owner) then
            owner:addEffect(effect, duration, amplifier)

            if grantToDragon then
                dragon:addEffect(effect, duration, amplifier)
            end
        end
    end
end