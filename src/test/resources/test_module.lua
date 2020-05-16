if test_module then
    return
end

test_module = {}

function test_module.calculate(arg)
    return arg * 10
end

return test_module