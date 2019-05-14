import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
import os
import numpy as np

f_path = "E:\\Github\\GVGAI-Sound\\audio\\img\\"
sounds = {"shoot": 0, "alienBomb": 10000, "killBaseAlien": -10000, "killBaseSam": 20000, "killAlien": -20000}
colors = {"shoot": 'r', "alienBomb": 'g', "killBaseSam": 'orange', "killAlien": 'b'}
scale = 200
maxX = 0

for i in range(50):
    sound = ""
    color = ""
    data = []

    for s in sounds:
        s_file = f_path + s + "_" + str(i) + ".txt"
        if os.path.isfile(s_file):
            with open(s_file) as f:
                lines = f.readlines()
                for line in lines:
                    spl = line.split(' ')
                    arrr = []
                    for idx in range(0, len(spl), 10):
                        n = spl[idx]
                        if n.strip() != '':
                            arrr.append(float(n) + sounds[s])
                    data.append(arrr)
                sound = s
                color = colors[s]

                for a in data:
                    maxx = scale*i + len(a)
                    x = list(range(scale*i, maxx))
                    plt.plot(x, a, color=color, label=sound, alpha=0.2)
                    if maxx > maxX:
                        maxX = maxx

h = []
for c in colors:
    h.append(mpatches.Patch(color=colors[c], label=c, alpha=0.5))
plt.legend(handles=h, loc='lower left', fontsize=12)

xlabels = []
xlocs = []
step = 100
skip = 9
for idx in range(maxX):
    if idx % step == 0:
        skip += 1
        if skip == 10:
            xlabels.append(idx/step)
            xlocs.append(idx)
            skip = 0

xlabels.append(step)
xlocs.append(maxX)

plt.xticks(xlocs, xlabels, fontsize=14)
plt.xlabel("Game tick", fontsize=16)
plt.ylabel("Amplitude", fontsize=16)

plt.show()

