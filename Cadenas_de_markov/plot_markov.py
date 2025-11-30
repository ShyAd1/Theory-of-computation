#!/usr/bin/env python3
"""
plot_markov.py
Lee un CSV generado por `cadenas_markov` (columnas: index,cumulative_v_fraction,cumulative_c_fraction,symbol)
y genera una gráfica de la fracción acumulada de `c` o `v`.

Uso:
  python plot_markov.py markov_output.csv [c|v] [--step]

Ejemplos:
  python plot_markov.py markov_output.csv       # grafica fracción de 'c' (por defecto)
  python plot_markov.py markov_output.csv v     # grafica fracción de vocales
  python plot_markov.py markov_output.csv c --step  # grafica con estilo escalonado (saltos)

Si matplotlib no está instalado, instala con:
  python -m pip install matplotlib
"""
import sys
import csv


def main():
    if len(sys.argv) < 2:
        print("Uso: python plot_markov.py markov_output.csv [c|v] [--step]")
        print(
            "  Por defecto grafica la fracción de 'c' (consonantes). Usa 'v' para vocales."
        )
        return 1

    fn = sys.argv[1]
    # If user doesn't pass a symbol, detect the first generated symbol from the CSV
    symbol = None
    step_plot = False
    autoscale = False
    zoom_n = 0
    # optional args: symbol and --step
    for a in sys.argv[2:]:
        if a.lower() in ("c", "v"):
            symbol = a.lower()
        if a == "--step":
            step_plot = True
            if a == "--autoscale":
                autoscale = True
        if a.startswith("--zoom"):
            # --zoom N or --zoom=N
            if a == "--zoom":
                # next arg expected, handled below
                continue
            if a.startswith("--zoom="):
                try:
                    zoom_n = int(a.split("=", 1)[1])
                except Exception:
                    zoom_n = 0

    idx = []
    vfrac = []
    cfrac = []
    first_sym = None

    try:
        with open(fn, newline="") as csvfile:
            rdr = csv.reader(csvfile)
            header = next(rdr, None)
            for row in rdr:
                if not row or len(row) < 3:
                    continue
                try:
                    i = int(row[0])
                    fv = float(row[1])
                    fc = float(row[2])
                except ValueError:
                    continue
                idx.append(i)
                vfrac.append(fv)
                cfrac.append(fc)
                # symbol column expected at index 3 if present
                if len(row) >= 4 and (first_sym is None):
                    s = row[3].strip().lower()
                    if s in ("c", "v"):
                        first_sym = s
    except FileNotFoundError:
        print(f"No se encontró el archivo: {fn}")
        return 1

    try:
        import matplotlib.pyplot as plt
    except Exception:
        print(
            "matplotlib no está disponible. Instálalo con: python -m pip install matplotlib"
        )
        return 1

    # If zoom_n is provided, draw two subplots: main (full-range) and zoom (first N samples)
    import matplotlib.pyplot as plt

    # If user didn't specify symbol via args, pick based on the first symbol in the CSV
    if symbol is None:
        if first_sym is not None:
            symbol = first_sym
        else:
            # fallback to consonant if CSV doesn't include symbol
            symbol = "c"

    if zoom_n > 0:
        fig, (ax_main, ax_zoom) = plt.subplots(
            2, 1, figsize=(10, 7), gridspec_kw={"height_ratios": [2, 1]}
        )
    else:
        fig, ax_main = plt.subplots(figsize=(10, 5))

    # choose which fraction to plot (single line)
    if symbol == "v":
        y = vfrac
        label = "v_fraction"
        color = "tab:blue"
    else:
        y = cfrac
        label = "c_fraction"
        color = "tab:orange"

    # Plot main axis
    if step_plot:
        ax_main.step(idx, y, where="post", color=color)
        ax_main.scatter(idx, y, s=8, color=color)
    else:
        ax_main.plot(idx, y, color=color)
        ax_main.scatter(idx, y, s=4, color=color)

    # Ensure y goes from 0 to 1 (probability scale) unless user requests autoscale.
    # Add a small visual margin so points at exactly 0.0 or 1.0 aren't clipped by the axes.
    if not autoscale:
        pad = 0.02
        y0 = 0.0 - pad
        y1 = 1.0 + pad
        ax_main.set_ylim(y0, y1)
        ax_main.margins(x=0.01)

    ax_main.set_xlabel("Index")
    ax_main.set_ylabel("Fraction")
    ax_main.set_title(f"Evolución fracción '{symbol}'")
    ax_main.grid(alpha=0.3)

    # If zoom requested, add zoom subplot showing first zoom_n points with autoscaled Y
    if zoom_n > 0:
        n = min(zoom_n, len(idx))
        if n <= 0:
            n = min(50, len(idx))
        xi = idx[:n]
        yi = y[:n]
        if step_plot:
            ax_zoom.step(xi, yi, where="post", label=f"first {n}", color=color)
            ax_zoom.scatter(xi, yi, s=10, color=color)
        else:
            ax_zoom.plot(xi, yi, label=f"first {n}", color=color)
            ax_zoom.scatter(xi, yi, s=6, color=color)

        ax_zoom.axhline(0.5, color="gray", linestyle="--", linewidth=0.8)
        # autoscale y for zoom area but add small pad relative to data range
        ymin = min(yi) if yi else 0.0
        ymax = max(yi) if yi else 1.0
        dr = ymax - ymin
        pad2 = dr * 0.05 if dr > 0 else 0.02
        ax_zoom.set_ylim(max(0.0, ymin - pad2), min(1.0, ymax + pad2))
        ax_zoom.set_xlabel("Index")
        ax_zoom.set_ylabel("Fraction")
        ax_zoom.grid(alpha=0.3)

    outpng = "markov_plot.png"
    plt.tight_layout()
    plt.savefig(outpng)
    print(f"Gráfica guardada en {outpng}")
    try:
        plt.show()
    except Exception:
        pass

    return 0


if __name__ == "__main__":
    sys.exit(main())
