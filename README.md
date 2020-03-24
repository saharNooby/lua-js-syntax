# lua-js-syntax

This is a [transpiler](https://en.wikipedia.org/wiki/Source-to-source_compiler) from an alternative JS-inspired syntax for Lua to vanilla Lua.

### Advantages:
- This is **not a Lua VM fork**, so you can use it with any VM implementation written in any language (like LuaJ for Java).
- Has syntax sugar that Lua is missing: continue, try-catch, throw, bitwise operators, compound operators, increments, decrements, arrow functions.
- You can use JS syntax highlighter to develop (given that you don't use Lua's OOP or # operator, which are invalid in normal JS)
- Converter can be run as a console app, optionally reading and writing to files.
- Line numbers are preserved during the conversion, so stacktraces will be accurate.

### Disadvantages:
- To run the converter from non-JVM language, you need to run it as a console app.
- To run LuaJS code, you need to convert it to Lua first. No runtime support like in Moonscript.
- LuaJS is not a strict ECMAScript subset since it has Lua-like OOP and # operator.

### Alternative projects

- [Moonscript](https://github.com/leafo/moonscript): a language compiled to Lua, with completely new syntax. The project itself provides a transpiler and runtime support for any Lua VM.
- [ljs](https://github.com/mingodad/ljs): C Lua VM fork with JS-like syntax.
- [jual](https://github.com/sajonoso/jual): C Lua VM fork with JS-like syntax that is a ECMAScript subset.
- [Killa](https://github.com/ex/Killa): C Lua VM fork with JS-like syntax.

## Comparison with Lua

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

// Function literals
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

let fTwoArgs = (x, y) => x + y

// if statement
if (10 > 1) {
    print('ten is more that one')
}

// while loop and break statement
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

// throw statement and try-catch block
try {
    if (1 > 10) {
        throw 'unexpected math behavior!'
    }
} catch (e) {
    print('some error has occurred: ' .. e)
}





// Math operators: +, -, *, /, %, ** (power operator)
let mathResult = 100 + 3 ** 2

// Logical operators: &&, ||
let conditionA = 10 > 1
let conditionB = 1 != 3
if (conditionA && conditionB) {
    print('all ok')
}

// Comparison operators
// >, <, >=, <=, ==, !=
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

// Increments and decrement statements
let incremented = 1
incremented++

// Ternary operator
let ternaryResult = 2 > 1 ? 'two is more than one' : 'something is wrong'

// OOP
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

-- Function literals
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

local fTwoArgs = function(x, y) return x + y end

-- if statement
if (10 > 1) then
    print('ten is more that one')
end

-- while loop and break statement
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

-- throw statement and try-catch block
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

-- Math operators: +, -, *, /, %, ** (power operator)
local mathResult = 100 + 3 ^ 2

-- Logical operators: &&, ||
local conditionA = 10 > 1
local conditionB = 1 ~= 3
if (conditionA and conditionB) then
    print('all ok')
end

-- Comparison operators
-- >, <, >=, <=, ==, !=
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

-- Increments and decrement statements
local incremented = 1
incremented = incremented + 1

-- Ternary operator
local ternaryResult = (2 > 1) and ('two is more than one') or ('something is wrong')

-- OOP
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

## Building

To build, you need:
- git
- Maven
- JDK 8+

`git clone` the repository, `cd` into its dir and run `mvn clean install`.

## Running

After build, you can either add this converter as a Maven dependency and use it from JVM-based language (Java, Kotlin, Scala etc.), or run it from console.

### Add as a dependency

Maven dependency:

```xml
<dependency>
    <groupId>me.saharnooby</groupId>
    <artifactId>lua-js-syntax</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

Alternatively, you can just add the shaded JAR as a dependency directly to your build system.

Then you can use method `me.saharnooby.luajssyntax.LuaJSToLua.convert(java.lang.String)` or `me.saharnooby.luajssyntax.LuaJSToLua.convert(java.lang.String, java.lang.Appendable)`. JavaDoc is available.

### Run from console

- `cd` into `target`
- Run `java -jar lua-js-syntax-1.0-SNAPSHOT-shaded.jar [source file] [destination file]`

If the source file was not specified, source code will be read from `stdin`.

If the destination file was not specified, resulting code will be written to `stdout`.

Any errors will result in non-zero exit code and stacktraces printed into `stderr`.

## Testing

The project has a big list of unit tests comparing LuaJS code behavior to the behavior of manually written equivalent Lua code.