#!/usr/bin/python3
import sys


class Main:

    def __init__(self, max_registers):
        self.input_list = []
        """Input list contain every read instruction (line). """

        self.aliases = {}
        """aliases is a dict with translation between name of var and address
        This dictionary contains pointers to memory or registers
        Key: name,
        val: Rx or DMx """
        self.regs = [None for i in range(max_registers)]
        """ regs representing registers in one fixed size list"""
        self.max_reg = max_registers
        """ limit of registers list """
        self.reg_idx = 0
        """ current position at registers array, if it reaches max size it will
         start from 0, it's like pointer in queue"""
        self.ram = []
        """ Ram is representing endless memory which contains names of vars"""
        self.args_round = [None for i in range(3)]
        """When is executing operation i.e. MUL ARG1 ARG2 ARG3, this list
        will keep ARG1 ARG2 ARG3 as registers, it prevent to move one of these
        register to RAM Memory when the next will be search free register
        Example:
            a = 1 * 2 - Used here (R0-2) registers the oldest was i.e. R0 - "a"
            
            b = a * 3 - We have "a" in R0, so go to next then we have to put
            "3" to register and 3 use "a" register and "a" going to RAM to DM0.
            After this we get wrong operation, because it would look like:
            MUL DM0 R0 R1
            but MUL operation take only register addresses.
            This list block that unwanted replacement.
            
            Size is 3 because the "longest operation" use 3 arguments
        """
        self.read_input()
        self.parse()

    def read_input(self):
        for line in sys.stdin:
            self.input_list.append(line.rstrip())

    @staticmethod
    def output(instruction):
        print(instruction)

    def reserve_register(self, var):
        """Return free register

           If there is no free register:
            - then get from the end of queue (oldest use) register
            - and put data to RAM memory

           if :var is register address it returns itself

           else if :var is a memory address, then it put value to register

           else :var contains number, then it return register name with that
           value
        """
        def get_free_oldest():
            # check if register is not reserved for THIS operation
            while self.regs[self.reg_idx] in self.args_round:
                self.reg_idx += 1
                if self.reg_idx == self.max_reg:
                    self.reg_idx = 0

            # extract var name and add it to RAM
            var_name = self.regs[self.reg_idx]
            self.ram.append(var_name)

            # update alias to DMx
            ram_idx = self.ram.index(var_name)
            self.aliases[var_name] = 'DM' + str(ram_idx)
            self.regs[self.reg_idx] = None          # clear register cell

            self.output('MOV R' + str(self.reg_idx) + ' DM' + str(ram_idx))

        if var[0] == 'R' and var[1:].isdigit():
            if self.regs[int(var[1:])] not in self.args_round:
                self.args_round.append(self.regs[int(var[1:])])
            return var

        elif var[0:2] == 'DM' and var[2:].isdigit():
            """Get register to move value from memory and clean memory cell"""

            if self.regs[self.reg_idx] is not None:
                get_free_oldest()

            self.output('MOV ' + str(var) + ' R' + str(self.reg_idx))

            # remove value from RAM
            name_of_var = self.ram[int(var[2:])]
            self.ram[int(var[2:])] = None

            # save name of variable to register
            self.regs[self.reg_idx] = name_of_var

            tmp_idx = self.reg_idx
            # reset pointer in queue to beginning
            self.reg_idx += 1
            if self.reg_idx == self.max_reg:
                self.reg_idx = 0

            if name_of_var not in self.args_round:
                self.args_round.append(name_of_var)
            return 'R' + str(tmp_idx)

        else:
            """ Variable name as var, get register"""
            if var not in self.regs:
                if self.regs[self.reg_idx] is not None:
                    get_free_oldest()

                # save name of var to register
                self.regs[self.reg_idx] = var

                tmp_idx = self.reg_idx
                # reset pointer in queue to beginning
                self.reg_idx += 1
                if self.reg_idx == self.max_reg:
                    self.reg_idx = 0

                if var not in self.args_round:
                    self.args_round.append(var)
                return 'R' + str(tmp_idx)
            else:
                if var not in self.args_round:
                    self.args_round.append(var)
                return 'R' + str(self.regs.index(var))

    def add_sub(self, line_list):
        """Void function which reserve register for args"""

        # A Parsing
        try:  # if number add to register
            int(line_list[2])
            self.aliases[str(line_list[2])] = self.reserve_register(str(line_list[2]))
            self.output('SET ' + self.aliases[str(line_list[2])]
                        + ' ' + str(line_list[2]))
        except ValueError:
            # check if variable is in register
            self.aliases[line_list[2]] = self.reserve_register(self.aliases[line_list[2]])

        # B Parsing
        try:  # if number add to register
            int(line_list[4])
            self.aliases[str(line_list[4])] = self.reserve_register(str(line_list[4]))
            self.output('SET ' + self.aliases[str(line_list[4])]
                        + ' ' + str(line_list[4]))
        except ValueError:
            # check if variable is in register
            self.aliases[line_list[4]] = self.reserve_register(self.aliases[line_list[4]])

        # SUM
        if line_list[0] not in self.aliases.keys():
            self.aliases[line_list[0]] = self.reserve_register(line_list[0])

    def mul_div_mod(self, line_list):
        """Return tuple (a, b) where a and b are registers"""

        # A Parsing
        try:  # if number add to register
            int(line_list[2])
            self.aliases[str(line_list[2])] = self.reserve_register(
                str(line_list[2]))
            self.output('SET ' + self.aliases[str(line_list[2])]
                        + ' ' + str(line_list[2]))
        except ValueError:
            self.aliases[line_list[2]] = self.reserve_register(self.aliases[line_list[2]])

        # B Parsing
        try:  # if number add to register
            int(line_list[4])
            self.aliases[str(line_list[4])] = self.reserve_register(
                str(line_list[4]))
            self.output('SET ' + self.aliases[str(line_list[4])]
                        + ' ' + str(line_list[4]))
        except ValueError:
            self.aliases[str(line_list[4])] = self.reserve_register(self.aliases[line_list[4]])

        if line_list[0] not in self.aliases.keys():
            self.aliases[line_list[0]] = self.reserve_register(line_list[0])

        return self.aliases[line_list[2]], self.aliases[line_list[4]]

    def parse(self):
        """Converting instructions into TCL code"""
        for line in self.input_list:
            self.args_round.clear()  # new operation - new block arguments
            line_list = line.split()
            len_of_list = len(line_list)

            if len_of_list == 1:            # always print(var)
                name = line_list[0][6:-1]   # extract name of var
                self.output('OUT ' + self.aliases[name])

            elif len_of_list == 3:          # always input x = int(input())
                self.aliases[line_list[0]] = self.reserve_register(line_list[0])
                self.output('IN ' + self.aliases[line_list[0]])

            elif len_of_list == 5:
                # ADDITION or INCREMENTATION
                if line_list[3] == '+':  # SUM = A + B
                    # if i.e. var = var + number INC operation
                    if line_list[0] == line_list[2]:
                        # be sure that var is not in RAM
                        self.aliases[line_list[0]] = self.reserve_register(self.aliases[line_list[0]])
                        self.output('INC ' + str(self.aliases[line_list[0]])
                                    + ' ' + str(line_list[4]))
                    # ADD Operation with using new registers to "temp" numbers
                    else:
                        self.add_sub(line_list)
                        self.output('ADD ' + str(self.aliases[line_list[2]])
                                    + ' ' + str(self.aliases[line_list[4]])
                                    + ' ' + str(self.aliases[line_list[0]]))

                # SUBTRACTION or DECREMENTATION
                elif line_list[3] == '-':
                    # if i.e. var = var - number DEC operation
                    if line_list[0] == line_list[2]:
                        # be sure that var is not in RAM
                        self.aliases[line_list[0]] = self.reserve_register(self.aliases[line_list[0]])
                        self.output('DEC ' + str(self.aliases[line_list[0]])
                                    + ' ' + str(line_list[4]))
                    # SUB Operation with using new registers to "temp" numbers
                    else:
                        self.add_sub(line_list)
                        self.output('SUB ' + str(self.aliases[line_list[2]])
                                    + ' ' + str(self.aliases[line_list[4]])
                                    + ' ' + str(self.aliases[line_list[0]]))

                # MULTIPLICATION
                elif line_list[3] == '*':
                    a, b = self.mul_div_mod(line_list)
                    self.output('MUL ' + a + ' ' + b + ' '
                                + str(self.aliases[line_list[0]]))

                # DIVISION
                elif line_list[3] == '//':
                    a, b = self.mul_div_mod(line_list)
                    self.output('DIV ' + a + ' ' + b + ' '
                                + str(self.aliases[line_list[0]]))

                # MODULO
                elif line_list[3] == '%':
                    a, b = self.mul_div_mod(line_list)
                    self.output('MOD ' + a + ' ' + b + ' '
                                + str(self.aliases[line_list[0]]))


Main(8)  # Start program with 8 registers
