-- sandbox --

-- Restrict version information
_VERSION = "Lua 5.2"

-- Remove dangerous globals
debug = nil          -- Debug library
dofile = nil         -- Execute a Lua script file
loadfile = nil       -- Load and execute a Lua script file
collectgarbage = nil -- Manually trigger garbage collection
os = nil             -- Operating system library
io = nil             -- Input/output library
package = nil        -- Lua package library (prevents dynamic module loading)

-- Restrict `load` and `loadstring` (used to execute Lua strings as code)
load = nil
loadstring = nil
