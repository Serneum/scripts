File out = new File("/Users/crr/workspace/uBuild/src/database/seed-data.sql")
File seedData = new File("/Users/crr/workspace/uBuild/src/database/seed-data.xml")
def rootNode = new XmlSlurper().parseText(seedData.text)

rootNode.table.each { table ->
    def tName = table.@name
    def columns = []
    table.column.each { column ->
        columns << column.text()
    }
    table.row.each { row ->
        def values = []
        row.children().each { value ->
            if (value.name() == "null") {
                values << "null"
            }
            else {
                def valueText = value.text()
                try {
                    def valueInt = Integer.valueOf(valueText)
                    values << valueInt
                }
                catch (Exception e) {
                    values << "'${valueText}'"
                }
            }
        }
        out << "INSERT INTO ${tName}(${columns.join(', ')})\n    VALUES(${values.join(', ')});;\n"
    }
    out << "\n"
}