# Comparison with vanilla Lua

Note that comments and indents are not preserved during the conversion, they were added to 
the Lua code manually.

<table><tr><th>LuaJS</th><th>Lua (converted and formatted)</th></tr>
<tr>
<td>

```javascript
// One-line comment
/* Multi-line
comment */

// Semicolons are optional
print('semicolon');
print('no semicolon')

// Global variables
gx, gy = 1, 2
gz = 3

// Local variables
let lx, ly = 4, 5

// Boolean literals and nil literal
let literals = [nil, true, false]

// String literals
let sx = "string with escaped characters:\r\n \u0123 \x01 \0"
let sy = 'single quotes\r\n'

// String concatenation
// Non-string values are automatically wrapped into tostring
let concatResult = 'values are: (' .. [1, 2, 3] .. ', ' .. 123 .. ')'

// Lists
let list = [1, 2, ['nested list', 3, 4]]

// Tables
let table = {key: 'value', nested: {x: 1, y: 2}, ['expression' .. ' as a key']: 3}
```

</td>
<td>

```lua
-- One-line comment
--[[ Multi-line
comment ]]--

-- Semicolons are optional
print('semicolon');
print('no semicolon')

-- Global variables
gx, gy = 1, 2
gz = 3

-- Local variables
local lx, ly = 4, 5

-- Boolean literals and nil literal
local literals = {nil, true, false}

-- String literals
local sx = "string with escaped characters:\r\n ģ \x01 \0"
local sy = 'single quotes\r\n'

-- String concatenation
-- Non-string values are automatically wrapped into tostring
local concatResult = 'values are: (' .. tostring({1, 2, 3}) .. ', ' .. tostring(123) .. ')'

-- Lists
local list = {1, 2, {'nested list', 3, 4}}

-- Tables
local table = {key='value', nested={x=1, y=2}, ['expression' .. ' as a key']=3}
```

</td>
</tr>
</table>

## Functions

<table><tr><th>LuaJS</th><th>Lua (converted and formatted)</th></tr>
<tr>
<td>

```javascript
function literalFunction() {
    return 1
}

function literalFunctionWithArgs(x, y) {
    return x + y
}

// Arrow functions
let f = () => 1

let fWithArg = x => x * 10

let fBlock = x => {
    return x * 10
}

let fWithTwoArgs = (x, y) => x + y
```

</td>
<td>

```lua
function literalFunction()
    return 1
end

function literalFunctionWithArgs(x, y)
    return x + y
end

-- Arrow functions
local f = function() return 1 end

local fWithArg = function(x) return x * 10 end

local fBlock = function(x)
    return x * 10
end

local fWithTwoArgs = function(x, y) return x + y end
```

</td>
</tr>
</table>

## Conditions and loops

<table><tr><th>LuaJS</th><th>Lua (converted and formatted)</th></tr>
<tr>
<td>

```javascript
// if statement, while loop and break statement
let counter = 10
while (true) {
    print(counter)

    counter--

    if (counter == 0) {
        break
    }
}

// do-while loop
counter = 10
do {
    print(counter)

    counter--
} while(counter > 1)

// continue statement and for loop (initialization; condition; action after iteration)
for (let i = 0; i < 10; i++) {
    print('i is ' .. i)

    if (i % 2 != 0) {
        continue
    }

    print('i is even')
}




// for-in loop
let table = {a: 1, b: 2, c: 3}

for (k in pairs(table)) {
    print('key only: ' .. k)
}

for (k, v in pairs(table)) {
    print('key and value: ' .. k .. ', ' .. v)
}

// for-of loop, iterates over ipairs
let list = [1, 2, 3]

for (v of list) {
    print('element is ' .. v)
}

for (v, i of list) {
    print('element is ' .. v .. ', index is ' .. i)
}

// Labels and goto
let result = 10
while (true) {
    if (result == 1) {
        goto someLabel
    }

    result--
}

someLabel: print(result)
```

</td>
<td>

```lua
-- if statement, while loop and break statement
local counter = 10
while (true) do
    print(counter)

    counter = counter - 1

    if (counter == 0) then
        break
    end
end

-- do-while loop
counter = 10
repeat
    print(counter)

    counter = counter - 1
until (not (counter > 1))

-- continue statement and for loop (initialization; condition; action after iteration)
do
    local i = 0;
    while (i < 10) do
        print('i is ' .. tostring(i))
        if (i % 2 ~= 0) then
            goto continueLabel
        end
        print('i is even')
        ::continueLabel::
        i = i + 1
    end
end

-- for-in loop
local table = {a=1, b=2, c=3}

for k in pairs(table) do
    print('key only: ' .. tostring(k))
end

for k, v in pairs(table) do
    print('key and value: ' .. tostring(k) .. ', ' .. tostring(v))
end

-- for-of loop, iterates over ipairs
local list = {1, 2, 3}

for _, v in ipairs(list) do
    print('element is ' .. tostring(v))
end

for i, v in ipairs(list) do
    print('element is ' .. tostring(v) .. ', index is ' .. tostring(i))
end

-- Labels and goto
local result = 10
while (true) do
    if (result == 1) then
        goto someLabel
    end
    result = result - 1
end

::someLabel::
print(result)
```

</td>
</tr>
</table>

## Exceptions

<table><tr><th>LuaJS</th><th>Lua (converted and formatted)</th></tr>
<tr>
<td>

```javascript
try {
    if (1 > 10) {
        throw 'unexpected math behavior!'
    }
} catch (e) {
    print('some error has occurred: ' .. e)
}




```

</td>
<td>

```lua
do
    local res_c3409912_0, e_c3409912_0 = pcall(function()
        if (1 > 10) then
            error('unexpected math behavior!')
        end
    end)
    if not res_c3409912_0 then
        local e = e_c3409912_0
        print('some error has occurred: ' .. tostring(e))
    end
end
```

</td>
</tr>
</table>

## Operators

<table><tr><th>LuaJS</th><th>Lua (converted and formatted)</th></tr>
<tr>
<td>

```javascript
// Math operators: +, -, *, /, %, ** (power operator)
let mathResult = 100 + 3 ** 2

// Logical operators: &&, ||
let conditionA = 10 > 1
let conditionB = 1 != 3
if (conditionA && conditionB) {
    print('all ok')
}

// Comparison operators: >, <, >=, <=, ==, !=
if (1 != 10) {
    print('all ok')
}

// Bitwise operators: & (and), | (or), ^ (xor), << (shift to left), >> (shift to right)
// This evaluates to 5:
let bitwiseResult = 1 & 2 | 4

// Unary operators: - (negation), ! (logical negation), ~ (bitwise not), # (length operator)
let notResult = ~4
let length = #[1, 2, 3]

// Compound assignment statements (works with all operators)
let compound = 100
compound *= 10
compound += 5

// Increment and decrement statements
let incremented = 1
incremented++

// Ternary operator
let ternaryResult = 2 > 1 ? 'two is more than one' : 'something is wrong'
```

</td>
<td>

```lua
-- Math operators: +, -, *, /, %, ** (power operator)
local mathResult = 100 + 3 ^ 2

-- Logical operators: &&, ||
local conditionA = 10 > 1
local conditionB = 1 ~= 3
if (conditionA and conditionB) then
    print('all ok')
end

-- Comparison operators: >, <, >=, <=, ==, !=
if (1 ~= 10) then
    print('all ok')
end

-- Bitwise operators: & (and), | (or), ^ (xor), << (shift to left), >> (shift to right)
-- This evaluates to 5:
local bitwiseResult = bit32.bor(bit32.band(1, 2), 4)

-- Unary operators: - (negation), ! (logical negation), ~ (bitwise not), # (length operator)
local notResult = bit32.bnot(4)
local length = #{1, 2, 3}

-- Compound assignment statements (works with all operators)
local compound = 100
compound = compound * 10
compound = compound + 5

-- Increment and decrement statements
local incremented = 1
incremented = incremented + 1

-- Ternary operator
local ternaryResult = (2 > 1) and ('two is more than one') or ('something is wrong')
```

</td>
</tr>
</table>

## OOP

<table><tr><th>LuaJS</th><th>Lua (converted and formatted)</th></tr>
<tr>
<td>

```javascript
SomeClass = {}

function SomeClass::new() {
  object = {}
  self.__index = self
  return setmetatable(object, self)
}

function SomeClass::printSomething() {
    print('something')
}

let someObject = SomeClass::new()
someObject::printSomething()
```

</td>
<td>

```lua
SomeClass = {}

function SomeClass:new()
    object = {}
    self.__index = self
    return setmetatable(object, self)
end

function SomeClass:printSomething()
    print('something')
end

local someObject = SomeClass:new()
someObject:printSomething()
```

</td>
</tr>
</table>