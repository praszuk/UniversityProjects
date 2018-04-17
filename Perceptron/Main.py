from math import exp
from random import randint, uniform

import tkinter as tk

import matplotlib
matplotlib.use('TkAgg')

from matplotlib.backends.backend_tkagg import FigureCanvasTkAgg
from matplotlib.figure import Figure
import matplotlib.patches as patches

# Square size
SIZE_FROM = -10
SIZE_TO = 10


class Computing:

    @staticmethod
    def generate_data(n_start=-1, n_end=1, c_start=SIZE_FROM, c_end=SIZE_TO,
                      num_of_points=100):
        """ This function generates random data for task.
        Points are generated in square.

        :param n_start: Start of neural weights range (including).
        :param n_end: End of neural weights range (including).
        :param c_start: Start of points range (including).
        :param c_end: End of of points range (including).
        :param num_of_points: Number of points in returned set.

        :returns: (w1, w2, bias, set_of_points (x, y))

        """
        w1, w2, b = (uniform(n_start, n_end) for _ in range(3))

        # set of random coordinates in range
        coordinates = set()
        while len(coordinates) != num_of_points:
            coordinates.add((randint(c_start, c_end), randint(c_start, c_end)))

        return w1, w2, b, coordinates

    @staticmethod
    def compute_data(w1, w2, b, coordinates):
        """
        :param w1: Weight 1 of neural vector.
        :param w2: Weight 2 of neural vector.
        :param b: Bias of neural vector.
        :param coordinates: Set of (x, y) tuples containing points coordinates.
        :returns: Tuple of coordinates and 2 function result, where:
        \t(x, y, unit_step_unipolar_result(), sigmoid_unipolar_result())
        """
        n = Neural(w1, w2, b)
        result = []

        for x, y in coordinates:
            result.append((x, y, n.unit_step_unipolar(x, y)
                               , n.sigmoid_unipolar(x, y)))
        return result

    @staticmethod
    def linear_function(w1, w2, b):
        """
        Linear function plot using w1, w2 and bias
        Comparing with equation of square walls from -10,-10 to 10, 10

        :param w1: weight1 of neural (a)
        :param w2: weight2 of neural (b)
        :param b: bias of neural     (c)
        :return: tuple of 2 list (x points, y points) / empty lists if w1=w2=0.
        """

        p_x, p_y = [], []
        if w1 == w2 == 0:
            return p_x, p_y
        elif w1 == 0:
            p_x.append(SIZE_FROM)
            p_x.append(SIZE_TO)
            p_y.append(-b / w2)
            p_y.append(-b / w2)
        elif w2 == 0:
            p_x.append(-b / w1)
            p_x.append(-b / w1)
            p_y.append(SIZE_FROM)
            p_y.append(SIZE_TO)
        else:
            # right wall i.e. x = 10
            if SIZE_FROM <= -(SIZE_TO * w1 + b) / w2 <= SIZE_TO:
                p_x.append(SIZE_TO)
                p_y.append(-(SIZE_TO * w1 + b) / w2)

            # left wall i.e. x = -10
            if SIZE_TO >= (SIZE_TO * w1 - b) / w2 >= SIZE_FROM:
                p_x.append(SIZE_FROM)
                p_y.append((SIZE_TO * w1 - b) / w2)

            # upper wall i.e. y = 10
            if SIZE_FROM <= -(SIZE_TO * w2 + b) / w1 <= SIZE_TO:
                p_y.append(SIZE_TO)
                p_x.append(-(SIZE_TO * w2 + b) / w1)

            # lower wall i.e. y = -10
            if SIZE_TO >= (SIZE_TO * w2 - b) / w1 >= SIZE_FROM:
                p_y.append(SIZE_FROM)
                p_x.append((SIZE_TO * w2 - b) / w1)

        return p_x, p_y


class Neural:
    def __init__(self, w1, w2, bias=0):
        self.w1 = w1
        self.w2 = w2
        self.bias = bias

    def __net(self, x, y):
        return self.w1 * x + self.w2 * y + self.bias

    def unit_step_unipolar(self, x, y):
        return 1 if self.__net(x, y) >= 0 else 0

    def sigmoid_unipolar(self, x, y):
        try:
            return 1 / (1 + exp(-self.__net(x, y)))
        except OverflowError:
            return 0.0


class Gui(tk.Frame):
    def __init__(self, master=None):
        self.master = master
        tk.Frame.__init__(self, self.master)
        self.winfo_toplevel().title('LAB02_8 - Paweł Raszuk s15225')

        self.label = tk.Label(self.master)
        self.label['text'] = 'w1:\nw2:\nbias:'
        self.label.grid(row=1, column=0)

        self.__create_plots_panel()
        self.generate_button = tk.Button(self.master,
                                         command=lambda: self.__update_plots(),
                                         text='Generate')
        self.generate_button.grid(row=2, column=0)
        self.master.mainloop()

    def __update_plots(self):
        w1, w2, b, c = Computing.generate_data()
        data = Computing.compute_data(w1, w2, b, c)

        # Updating label
        self.label['text'] = 'w1: {0}\nw2: {1}\nbias: {2}'.format(w1, w2, b)

        # basic lines on plot
        p_x, p_y = Computing.linear_function(w1, w2, b)
        for ax in [self.axis1, self.axis2]:
            ax.clear()

            # Square as border
            ax.add_patch(patches.Rectangle((SIZE_FROM, SIZE_FROM),
                                           SIZE_TO * 2, SIZE_TO * 2,
                                           fill=False))
            # linear function of neural
            if len(p_x) and len(p_y):
                ax.plot(p_x, p_y, linestyle='--', c='gray')

        # drawing points
        for x, y, res1, res2 in data:
            # axis 1
            if res1 == 0:
                self.axis1.scatter(x, y, c='yellow')
            else:
                self.axis1.scatter(x, y, c='black')

            # axis 2
            if 0.0 <= res2 < 0.25:
                self.axis2.scatter(x, y, c='yellow')
            elif 0.25 <= res2 < 0.5:
                self.axis2.scatter(x, y, c='green')
            elif 0.5 <= res2 < 0.75:
                self.axis2.scatter(x, y, c='blue')
            elif 0.75 <= res2 <= 1:
                self.axis2.scatter(x, y, c='red')

        self.canvas.draw()

    def __create_plots_panel(self):
        f = matplotlib.figure.Figure(figsize=(10, 5))

        # 2 plots in GUI
        self.axis1, self.axis2 = f.subplots(1, 2)

        self.canvas = FigureCanvasTkAgg(f, master=self.master)
        self.canvas.get_tk_widget().grid(row=0, column=0)
        self.canvas.draw()


if __name__ == "__main__":
    g = Gui()
