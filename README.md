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
- LuaJS is not a strict ECMAScript subset since it has Lua-like OOP, `#` and `..` operators.

### Alternative projects

- [Moonscript](https://github.com/leafo/moonscript): a language compiled to Lua, with completely new syntax. The project itself provides a transpiler and runtime support for any Lua VM.
- [ljs](https://github.com/mingodad/ljs): C Lua VM fork with JS-like syntax.
- [jual](https://github.com/sajonoso/jual): C Lua VM fork with JS-like syntax that is a ECMAScript subset.
- [Killa](https://github.com/ex/Killa): C Lua VM fork with JS-like syntax.

## Comparison with Lua

<table><tr><th>LuaJS</th><th>Lua</th></tr>
<tr>
<td>

```javascript
/* A simple division function. */
let divide = (x, y) => {
    if (y == 0) {
        throw "Divisor is zero"
    }
    return x / y
}

try {
    print('Result is ' .. divide(10, 0))
} catch (e) {
    print('Error: ' .. e)
}

```

</td>
<td>

```lua
--[[ A simple division function. ]]--
local divide = function(x, y)
    if (y == 0) then
        error("Divisor is zero")
    end
    return x / y
end

local res, e = pcall(function()
    print('Result is ' .. tostring(divide(10, 0)))
end)
if not res then
    print('Error: ' .. tostring(e))
end
```

</td>
</tr>
</table>

See [COMPARISON.md](https://github.com/saharNooby/lua-js-syntax/blob/master/COMPARISON.md) for full comparison.

## Building

To build, you need:
- git
- [JDK 8 or higher](https://adoptopenjdk.net/)
- [Maven](http://maven.apache.org/)

This will put JAR file to `target` dir and install it into local Maven repository:

```shell script
git clone https://github.com/saharNooby/lua-js-syntax.git
cd lua-js-syntax
mvn clean install
```

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

Any errors will result in non-zero exit code and stacktraces printed to `stderr`.

## Testing

The project has a list of unit tests comparing LuaJS code behavior to the behavior of manually written equivalent Lua code.