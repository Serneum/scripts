def convertToBase(int decimal, int base) {
    String result = ""
    int multiples = Integer.MAX_VALUE
    while (multiples > 0) {
        multiples = decimal / base
        int remainder = decimal % base
        result = remainder + result
        decimal = multiples
    }
    return Integer.parseInt(result)
}

def convert0Result = convertToBase(0, 7)
def convert1Result = convertToBase(1, 8)
def b8Result = convertToBase(100, 8)
def b9Result = convertToBase(100, 9)

assert convert0Result == 0
assert convert1Result == 1
assert b8Result == 144
assert b9Result == 121
