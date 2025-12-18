-- KEYS[1] : ZSET 키
-- ARGV[1] : tauMillis
-- ARGV[2] : intervalMillis
-- ARGV[3] : epsilon
-- ARGV[4..] : [member1, incScore1, member2, incScore2, ...]

local zsetKey      = KEYS[1]
local tauMillis    = tonumber(ARGV[1])
local intervalMillis = tonumber(ARGV[2])
local epsilon      = tonumber(ARGV[3])

-- decay factor 계산
local decayFactor = math.exp(-intervalMillis / tauMillis)

-- 1) 기존 점수 감쇠
local members = redis.call('ZRANGE', zsetKey, 0, -1, 'WITHSCORES')
for i = 1, #members, 2 do
    local member = members[i]
    local score  = tonumber(members[i + 1])

    if score ~= nil then
        local decayed = score * decayFactor
        if decayed < epsilon then
            redis.call('ZREM', zsetKey, member)
        else
            redis.call('ZADD', zsetKey, decayed, member)
        end
    end
end

-- 2) 새 배치의 증가분 반영 (keyword, count 페어)
local argLen = #ARGV
if argLen > 3 then
    local i = 4
    while i <= argLen do
        local member = ARGV[i]
        local inc    = tonumber(ARGV[i + 1])
        if inc ~= nil and inc > 0 then
            redis.call('ZINCRBY', zsetKey, inc, member)
        end
        i = i + 2
    end
end

return 1