#!/usr/bin/python3
import numpy as np
from math import exp


class Neural:
    """
    :param wv: Weight vector for neural as list i.e. [w1, w2,..., wn] integers.
    :param b: Bias integer.
    """
    def __init__(self, wv, b):
        self.wv = wv
        self.b = b

    def net(self, vector):
        return np.sum(np.multiply(self.wv, vector)) + self.b

    @staticmethod
    def unit_step_unipolar(net):
        return 1 if net >= 0 else 0

    @staticmethod
    def sigmoid_unipolar(net):
        try:
            return 1 / (1 + exp(-net))
        except OverflowError:
            return 0.0


class SuperVisedLearning:
    """
    Algorithm, which using data_sets and expected result, learns neural.
    It does the following steps:

    1. For each learning data vector (ld[0]): Computing NET for neural.
        Then get y from NET as Activation function result.
    2. Update neural weights and bias using this formulas

        - Weights = weight + lr*(ld[1] - y)*ld[0]
        - Bias = bias + lr*(ld[1] - y)

    3. If (Error < Error_min) OR (Generation > Gen_max), then stop else go to 2

        - Error = 1/2 * Sum_i=1 -> n (ld[1]_i-y_i)^2


    :type n: Neural
    :param lr: Learning rate for algorithm. Value between 0-1 (exclude).
    :param ld: Learning data for algorithm. Tuple of tuples.
        Each tuple contains (vector of input(as tuple), expected_result).
    :param e_min: Error minimum value. If algorithm learns more than the e_min,
        then it will stop. If value == -1 this condition is ignored*.
    :param gen_max: If algorithm exceed generations max. It will stop. If
        value == -1 this condition is ignored*.

    *Note: At least one of (e_min, gen_max) must be set.
    """
    def __init__(self, n, lr, ld, e_min=0, gen_max=5):
        self.n = n
        self.lr = lr
        self.ld = ld
        self.e_min = e_min
        self.gen_max = gen_max

    def start_learning(self, print_output=True):
        gen = 1

        # Each iteration is new Generation
        while True:
            if print_output:
                print('*'*40)
                print('Generation #{0}'.format(gen))

            # Update weights and bias
            y = [self.n.unit_step_unipolar(self.n.net(d[0])) for d in self.ld]
            for d, y in zip(self.ld, y):

                # W = W + lr*(d-y)*X - Weights
                _part = self.lr*(d[1] - y) * np.array(d[0])
                self.n.wv = np.add(self.n.wv, _part)

                # B = B + lr*(d-y) - Bias
                self.n.b = self.n.b + self.lr * (d[1] - y)

                if print_output:
                    print('-> y: {0}'
                          '\n-> w: {1}'
                          '\n-> b: {2}'.format(y, self.n.wv, self.n.b))
                    print('='*40)

            # Get Error. Error = 1/2 * sum(d_i - y_i)^2
            y = [self.n.unit_step_unipolar(self.n.net(d[0])) for d in self.ld]
            e = 0

            for d, y in zip(self.ld, y):
                e += (d[1] - y) ** 2
            e /= 2

            if print_output:
                print('-> e: {0}'.format(e))

            # Stop condition
            if ((gen >= self.gen_max) and (self.gen_max != -1)) \
                    or ((e <= self.e_min) and (self.e_min != -1)):
                break

            gen += 1


if __name__ == "__main__":
    neural = Neural([-1, 2, 1], -2)
    data_set = (([-1, 0, 3], 0), ([1, 0, -2], 1), ([-3, -2, -1], 1))
    s = SuperVisedLearning(neural, 0.5, data_set, e_min=0, gen_max=5)
    s.start_learning()
