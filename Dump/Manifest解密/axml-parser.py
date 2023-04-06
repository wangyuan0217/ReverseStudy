class TreeNode(object):
    def __init__(self,attr_id,attr_value):
        self.Id = attr_id
        self.Value = attr_value
        self.Children = []
        self.Parent = None
        self.XML_list = []
        self.XML = ""

class AxmlParser(object):
    def __init__(self, file_name):
        self.__buf = open(file_name, "rb").read()
        self.__index = 0

        self.__AttributeType = {}
        self.__AttributeType["TYPE_NULL"] = 0
        self.__AttributeType["TYPE_REFERENCE"] = 1
        self.__AttributeType["TYPE_ATTRIBUTE"] = 2
        self.__AttributeType["TYPE_STRING"] = 3
        self.__AttributeType["TYPE_FLOAT"] = 4
        self.__AttributeType["TYPE_DIMENSION"] = 5
        self.__AttributeType["TYPE_FRACTION"] = 6
        self.__AttributeType["TYPE_FIRST_INT"] = 16
        self.__AttributeType["TYPE_INT_DEC"] = 16
        self.__AttributeType["TYPE_INT_HEX"] = 17
        self.__AttributeType["TYPE_INT_BOOLEAN"] = 18
        self.__AttributeType["TYPE_FIRST_COLOR_INT"] = 28
        self.__AttributeType["TYPE_INT_COLOR_ARGB8"] = 28
        self.__AttributeType["TYPE_INT_COLOR_RGB8"] = 29
        self.__AttributeType["TYPE_INT_COLOR_ARGB4"] = 30
        self.__AttributeType["TYPE_INT_COLOR_RGB4"] = 31
        self.__AttributeType["TYPE_LAST_COLOR_INT"] = 31
        self.__AttributeType["TYPE_LAST_INT"] = 31

        self.__ComplexUnit = {}
        self.__ComplexUnit["COMPLEX_UNIT_PX"] = 0
        self.__ComplexUnit["COMPLEX_UNIT_DIP"] = 1
        self.__ComplexUnit["COMPLEX_UNIT_SP"] = 2
        self.__ComplexUnit["COMPLEX_UNIT_PT"] = 3
        self.__ComplexUnit["COMPLEX_UNIT_IN"] = 4
        self.__ComplexUnit["COMPLEX_UNIT_MM"] = 5
        self.__ComplexUnit["COMPLEX_UNIT_SHIFT"] = 0
        self.__ComplexUnit["COMPLEX_UNIT_MASK"] = 15
        self.__ComplexUnit["COMPLEX_UNIT_FRACTION"] = 0
        self.__ComplexUnit["COMPLEX_UNIT_FRACTION_PARENT"] = 1
        self.__ComplexUnit["COMPLEX_RADIX_23p0"] = 0
        self.__ComplexUnit["COMPLEX_RADIX_16p7"] = 1
        self.__ComplexUnit["COMPLEX_RADIX_8p15"] = 2
        self.__ComplexUnit["COMPLEX_RADIX_0p23"] = 3
        self.__ComplexUnit["COMPLEX_RADIX_SHIFT"] = 4
        self.__ComplexUnit["COMPLEX_RADIX_MASK"] = 3
        self.__ComplexUnit["COMPLEX_MANTISSA_SHIFT"] = 8
        self.__ComplexUnit["COMPLEX_MANTISSA_MASK"] = 0xFFFFFF

        self.__string_pool = []
        self.__ns_index_finger = -1
        self.__xml_list = []
        self.__root = None

    def __get_1_byte(self):
        return self.__get_bytes_by_length(1)

    def __get_2_bytes(self):
        return self.__get_bytes_by_length(2)

    def __get_4_bytes(self):
        return self.__get_bytes_by_length(4)

    def __get_string(self, len, char_format):
        s = []

        if char_format != 0:
            len &= 0xff
        else:
            len = len * 2

        for i in range(0, len):
            s.append(self.__get_1_byte())

        b = bytes(s)
        ret = ""
        try:
            ret = b.decode().replace("\x00", "")
        except:
            ret = str(b)

        return ret

    def __get_bytes_by_length(self, length):
        r = 0
        for i in range(length, 0, -1):
            r += (self.__buf[self.__index + i - 1] << (8 * i - 8))

        self.__index += length
        return r

    def __get_headers_stringpools_resources(self):
        magic_number_always_0x08000300_or_134218496 = self.__get_4_bytes()
        file_size = self.__get_4_bytes()
        string_tag_always_0x1c0001_or_1835009 = self.__get_4_bytes()
        sizeof_string_chunk = self.__get_4_bytes()
        numberof_strings = self.__get_4_bytes()
        numberof_styles = self.__get_4_bytes()
        char_format = self.__get_4_bytes()
        start_offset_of_string = self.__get_4_bytes()
        start_offset_of_styles = self.__get_4_bytes()

        start_offset = self.__index
        string_offset_list = []
        for i in range(0, numberof_strings):
            string_offset_list.append(self.__get_4_bytes() + start_offset_of_string + 0x08)
            if 4 * i + start_offset >= string_offset_list[0]:
                numberof_strings = i  # 修正个数
                break

        styles_offset_list = []
        for i in range(0, numberof_styles):
            styles_offset_list.append(self.__get_4_bytes() + start_offset_of_styles + 0x08)

        for i in range(0, numberof_strings):
            self.__index = string_offset_list[i]

            str_len = self.__get_2_bytes()
            s = self.__get_string(str_len, char_format)
            self.__string_pool.append(s)

        if char_format != 0:
            self.__index += 1
        else:
            self.__index += 2
        resource_tag_always_0x080180_or_524672 = self.__get_4_bytes()
        sizeof_resource_chunk = self.__get_4_bytes()

        numberof_resource_chunks = int(sizeof_resource_chunk / 4) - 2
        resource_offset_list = []
        for i in range(0, numberof_resource_chunks):
            resource_offset_list.append(self.__get_4_bytes())

        return file_size

    def Deserialize(self):
        file_size = self.__get_headers_stringpools_resources()
        self.__xml_list.append('<?xml version="1.0" encoding="utf-8" ?>')

        current_node = None
        while self.__index < file_size:
            tag_chunk = self.__get_4_bytes()

            match tag_chunk:
                case 0x00100100:
                    self.__ns_index_finger = self.__process_start_namespace_chunk(current_node)
                case 0x00100101:
                    self.__process_end_namespace_chunk(current_node)
                case 0x00100102:
                    current_node =self.__process_start_tag_chunk(current_node)
                case 0x00100103:
                    current_node =self.__process_end_tag_chunk(current_node).Parent
                case 0x00100104:
                    self.__process_text_chunk()
                case _:
                    raise Exception("Invalid tag chunk value.")

        self.__recursive_get_xml_from_treenode(self.__root, 0)
        return '\n'.join(self.__xml_list)

    def __recursive_get_xml_from_treenode(self, node:TreeNode, depth:int):
        prefix = "\t"*depth
        if len(node.Children)==0:
            self.__xml_list.append(prefix + node.Value[:-1] + "/>")
        else:
            self.__xml_list.append(prefix + node.Value)

        depth += 1
        for child in node.Children:
            self.__recursive_get_xml_from_treenode(child, depth)
        depth -= 1

        if len(node.Children)>0:
            self.__xml_list.append("{0}</{1}>".format("\t" * depth, node.Id))

    def __process_start_namespace_chunk(self, current_node):
        signature = self.__get_4_bytes()
        size = self.__get_4_bytes
        line_number = self.__get_4_bytes()
        unknown = self.__get_4_bytes()
        prefix = self.__get_4_bytes()
        return self.__get_4_bytes()

    def __process_end_namespace_chunk(self, current_node):
        signature = self.__get_4_bytes()
        size = self.__get_4_bytes
        line_number = self.__get_4_bytes()
        unknown = self.__get_4_bytes()
        prefix = self.__get_4_bytes()
        uri = self.__get_4_bytes()

    def __process_start_tag_chunk(self, current_node):
        signature = self.__get_4_bytes()
        size = self.__get_4_bytes
        line_number = self.__get_4_bytes()
        unknown = self.__get_4_bytes()
        namespace_uri = self.__get_4_bytes()
        element_index = self.__get_4_bytes()
        flags = self.__get_4_bytes()
        attribute_count = self.__get_4_bytes() & 0x0000ffff
        class_attribute = self.__get_4_bytes()

        element_name = self.__string_pool[element_index]
        xml = "<"
        xml += element_name
        if self.__string_pool[element_index]== "manifest":
            xml += ' xmlns:android="http://schemas.android.com/apk/res/android"'

        for i in range(0, attribute_count):
            xml += " "

            attr_namespace_uri = self.__get_4_bytes()
            attr_name = self.__get_4_bytes()
            attr_value_str = self.__get_4_bytes()
            attr_type = self.__get_4_bytes() >> 24
            attr_data = self.__get_4_bytes()

            if attr_namespace_uri == self.__ns_index_finger:
                xml += "android:"

            xml += self.__string_pool[attr_name]
            xml += ' = "'
            attr_value = self.__get_attribute_value(attr_type, attr_data)
            xml += str(attr_value)
            xml += '"'

        xml += ">"

        node = TreeNode(element_name, xml)

        if current_node is None:
            self.__root = node
        else:
            node.Parent = current_node
            current_node.Children.append(node)

        return node

    def __process_end_tag_chunk(self, current_node):
        signature = self.__get_4_bytes()
        size = self.__get_4_bytes
        line_number = self.__get_4_bytes()
        unknown = self.__get_4_bytes()
        namespace_uri = self.__get_4_bytes()
        names = self.__get_4_bytes()

        return current_node

    def __process_text_chunk(self):
        signature = self.__get_4_bytes()
        size = self.__get_4_bytes
        line_number = self.__get_4_bytes()
        unknown = self.__get_4_bytes()
        text_index = self.__get_4_bytes()
        unknown = self.__get_4_bytes()
        unknown = self.__get_4_bytes()

    def __complexToFloat(self, complex):
        RADIX_MULTS = [0.00390625, 3.051758E-005, 1.192093E-007, 4.656613E-010]
        return (float)(complex & 0xFFFFFF00) * RADIX_MULTS[(complex >> 4) & 3]

    def __get_attribute_value(self, attr_type, data):
        DIMENSION_UNITS = ["px", "dip", "sp", "pt", "in", "mm", "", ""]
        FRACTION_UNITS = ["%", "%p", "", "", "", "", "", ""]

        if attr_type == self.__AttributeType['TYPE_STRING']:
            return self.__string_pool[data].strip()
        elif attr_type == self.__AttributeType['TYPE_REFERENCE']:
            return "@" + str(data)
        elif attr_type == self.__AttributeType["TYPE_ATTRIBUTE"]:
            return "?" + str(data)
        elif attr_type == self.__AttributeType["TYPE_FLOAT"]:
            return float(data)
        elif attr_type == self.__AttributeType["TYPE_DIMENSION"]:
            return str(self.__complexToFloat(data)) + DIMENSION_UNITS[
                (data >> self.__ComplexUnit["COMPLEX_UNIT_SHIFT"]) & self.__ComplexUnit["COMPLEX_UNIT_MASK"]]
        elif attr_type == self.__AttributeType["TYPE_FRACTION"]:
            return str(self.__complexToFloat(data) * 100) + FRACTION_UNITS[
                (data >> self.__ComplexUnit["COMPLEX_UNIT_SHIFT"]) & self.__ComplexUnit["COMPLEX_UNIT_MASK"]]
        elif attr_type == self.__AttributeType["TYPE_INT_HEX"]:
            return "0x{:08x}".format(data).upper()
        elif attr_type == self.__AttributeType["TYPE_INT_BOOLEAN"]:
            return "false" if data == 0 else "true"

        if (attr_type >= self.__AttributeType["TYPE_FIRST_COLOR_INT"] and attr_type <= self.__AttributeType["TYPE_LAST_COLOR_INT"]):
            res = "{:08x}".format(data).upper()

            if attr_type == self.__AttributeType["TYPE_INT_COLOR_RGB8"]:  # FFRrGgBb->#RrGgBb
                res = res[2:]
            elif attr_type == self.__AttributeType["TYPE_INT_COLOR_ARGB4"]:  # # AARRGGBB->#ARGB
                res = res[0] + res[2] + res[4] + res[6]
            elif attr_type == self.__AttributeType["TYPE_INT_COLOR_RGB4"]:  # FFRRGGBB->#RGB
                res = res[0] + res[2] + res[4] + res[6]

            return "#" + res
        elif (attr_type >= self.__AttributeType["TYPE_FIRST_INT"] and attr_type <= self.__AttributeType["TYPE_LAST_INT"]):
            return str(data)

        raise Exception("Incorrect type id.")

def main():
    p = AxmlParser("/Users/admin/Downloads/AndroidManifest.xml")
    axml = p.Deserialize()
    with open("AndroidManifest-Clean.xml","w",encoding="utf-8") as f:
        f.writelines(axml)

if __name__ == "__main__":
    main()
