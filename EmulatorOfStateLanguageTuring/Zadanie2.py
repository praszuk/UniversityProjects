#!/bin/python3
import sys

if __name__ == "__main__":
    states = {}                         # dict of available instructions/states
    input_data = []                     # list of input data in order

    with open(sys.argv[1]) as file:     # read from file, path as argument
        next(file)                      # skip first line, not need this number

        for line in file:
            if line.strip():            # ignore empty lines
                if line[0] == '/':      # ignore comments lines
                    continue
                word = line.split()
                if line[0] == '$':
                    # adding list of characters not string to easier inserting
                    input_data.append(list(word[1]))
                else:
                    states[(word[0], word[1])] = (word[3], word[4], word[5])

    # start program
    for data in input_data:

        current_index = 0
        max_index = len(data) - 1

        current_state = states[('START', data[current_index])]
        while current_state[0] != 'STOP':
            # print('Current ' + str(data))          # debug of state
            data[current_index] = current_state[1]   # replace character

            # move
            if current_state[2] == 'left':
                if current_index != 0:
                    current_index -= 1
                else:
                    data.insert(0, '#')
                    max_index += 1

            else:   # right
                if current_index != max_index:
                    current_index += 1
                else:
                    data.append('#')
                    max_index += 1
                    current_index += 1

            # change state
            current_state = states[(current_state[0], data[current_index])]

        # STOP
        data[current_index] = current_state[1]

        # printing
        # print(data)                                   # debug of result
        start_index = -1
        end_index = -1

        for index, elem in enumerate(data):             # search start index
            if elem == '1' or elem == '0':
                start_index = index
                break

        for index, elem in enumerate(reversed(data)):   # search end index
            if elem == '1' or elem == '0':
                end_index = max_index-index + 1
                break

        print(''.join(data[start_index:end_index]))     # substring & to string
