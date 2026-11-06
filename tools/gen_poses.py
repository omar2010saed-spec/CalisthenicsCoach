#!/usr/bin/env python3
"""Generates Poses.kt (joint coordinates) from joint angles with real body proportions, and renders
contact sheets for visual checking.

Usage:
  python3 -I tools/gen_poses.py kotlin   > app/src/main/java/com/calisthenics/coach/data/Poses.kt
  python3 -I tools/gen_poses.py preview <outdir>

Side view, figure faces right. Angle a is the global direction of a segment: dir(a) = (sin a, cos a) with y down,
so 0 = down, 90 = forward (right), 180 = up, 270 = backward (left).
"""
import math
import re
import sys

# segment lengths as fractions of body height
T, N, UA, FA, HD, TH, SH, FT = 0.30, 0.13, 0.186, 0.146, 0.05, 0.245, 0.246, 0.10
SCALE = 0.85


def d(a, l):
    r = math.radians(a)
    return (math.sin(r) * l, math.cos(r) * l)


def add(p, v):
    return (p[0] + v[0], p[1] + v[1])


def build(torso=180, uarm=0, farm=0, thigh=0, shin=0, foot=None, head=None, far=None, lift=0.0, shift=-0.035):
    far = far or {}
    hip = (0.0, 0.0)
    sh = add(hip, d(torso, T))
    hd = add(sh, d(torso if head is None else head, N))
    el = add(sh, d(uarm, UA))
    hand = add(el, d(farm, FA + HD))
    kn = add(hip, d(thigh, TH))
    an = add(kn, d(shin, SH))
    toe = add(an, d(shin + 90 if foot is None else foot, FT))
    if "arm" in far:
        ua2, fa2 = far["arm"]
        el2 = add(sh, d(ua2, UA))
        hand2 = add(el2, d(fa2, FA + HD))
    else:
        el2 = (el[0] + shift, el[1])
        hand2 = (hand[0] + shift, hand[1])
    if "leg" in far:
        th2, sh2 = far["leg"]
        f2 = far.get("foot", sh2 + 90)
        kn2 = add(hip, d(th2, TH))
        an2 = add(kn2, d(sh2, SH))
        toe2 = add(an2, d(f2, FT))
    else:
        kn2 = (kn[0] + shift, kn[1])
        an2 = (an[0] + shift, an[1])
        toe2 = (toe[0] + shift, toe[1])
    return [hd, sh, el, hand, hip, kn, an, toe, el2, hand2, kn2, an2, toe2], lift


def solve(fn, lo, hi, i, j):
    """Find parameter t in [lo, hi] where joint i and joint j are at the same height (y)."""
    def g(t):
        pts, _ = fn(t)
        return pts[i][1] - pts[j][1]
    n = 400
    best = None
    prev_t, prev_g = lo, g(lo)
    for k in range(1, n + 1):
        t = lo + (hi - lo) * k / n
        gt = g(t)
        if prev_g == 0 or prev_g * gt < 0:
            a, b = prev_t, t
            for _ in range(40):
                m = (a + b) / 2
                if g(a) * g(m) <= 0:
                    b = m
                else:
                    a = m
            return fn((a + b) / 2)
        if best is None or abs(gt) < best[0]:
            best = (abs(gt), t)
        prev_t, prev_g = t, gt
    return fn(best[1])


POSES = {}


def reg(name, value):
    POSES[name] = value


HAND, TOE, KNEE = 3, 7, 5

# ---------------------------------------------------------------- standing
reg("STAND", build(180, 0, 0, 0, 0, 90))
reg("CALF_DOWN", build(180, 0, 0, 0, 0, 90))
reg("ARMS_UP", build(180, 180, 180, 0, 0, 90))
reg("STRETCH_REACH", build(178, 180, 180, 0, 0, 90))
reg("ARMS_FRONT", build(180, 90, 90, 0, 0, 90))
reg("ARMS_OUT", build(180, 40, 140, 0, 0, 90))
reg("CURL_DOWN", build(180, 0, 0, 0, 0, 90))
reg("CURL_UP", build(180, 0, 160, 0, 0, 90))
reg("KICK_BACK_A", build(130, 270, 0, 10, 350, None, head=105))
reg("KICK_BACK_B", build(130, 270, 270, 10, 350, None, head=105))
reg("SQUAT_UP", build(180, 90, 90, 0, 0, 90))
reg("SQUAT_DOWN", build(145, 90, 90, 80, 340, 90, head=150))
reg("CHAIR_SIT", build(180, 90, 90, 90, 0, 90))
reg("LUNGE_UP", build(180, 330, 30, 0, 0, 90))
reg("LUNGE_DOWN", build(180, 330, 30, 80, 0, 90, far={"leg": (335, 255), "foot": 10}))
reg("HINGE", build(120, 0, 0, 10, 350, None, head=130))
reg("LEG_BACK", build(80, 0, 0, 358, 2, 90, head=85, far={"leg": (260, 260), "foot": 250}))
reg("CALF_UP", build(180, 0, 0, 0, 0, 45))
reg("JUMP", build(180, 160, 160, 0, 0, 10, lift=0.14))
reg("WALL_FAR", build(162, 90, 90, 342, 342, 90, head=162))
reg("WALL_NEAR", build(145, 330, 90, 325, 325, 90, head=145))
reg("MARCH_A", build(180, 320, 350, 100, 0, 90, far={"arm": (40, 70), "leg": (0, 0), "foot": 90}))
reg("MARCH_B", build(180, 40, 70, 0, 0, 90, far={"arm": (320, 350), "leg": (100, 0), "foot": 90}))
reg("QUAD_STRETCH", build(180, 340, 330, 0, 250, 250, far={"leg": (0, 0), "foot": 90}))
reg("PISTOL", build(145, 90, 90, 80, 340, 90, head=150, far={"leg": (90, 90), "foot": 180}))
reg("HIP_L", build(172, 330, 30, 4, 356, 90))
reg("HIP_R", build(188, 330, 30, 356, 4, 90))
reg("WRIST_A", build(180, 90, 70, 0, 0, 90))
reg("WRIST_B", build(180, 90, 125, 0, 0, 90))
reg("SWING_F", build(180, 90, 90, 40, 40, 130, far={"leg": (0, 0), "foot": 90}))
reg("SWING_B", build(180, 90, 90, 330, 330, 40, far={"leg": (0, 0), "foot": 90}))
reg("CHEST_A", build(180, 340, 340, 0, 0, 90))
reg("CHEST_B", build(180, 300, 290, 0, 0, 90))
reg("KNEEL_A", build(180, 330, 30, 90, 0, 90, far={"leg": (0, 270), "foot": 270}))
reg("KNEEL_B", build(180, 180, 180, 90, 0, 90, far={"leg": (0, 270), "foot": 270}))

# ---------------------------------------------------------------- plank family (solved so hands and toes touch the floor)
reg("PLANK_HIGH", solve(lambda t: build(90 + t, 0, 0, 270 + t, 270 + t, 20, head=65 + t), 0, 45, HAND, TOE))
reg("PUSHUP_DOWN", solve(lambda t: build(90 + t, 235, 0, 270 + t, 270 + t, 20, head=65 + t), 0, 45, HAND, TOE))
reg("ARCHER_A", POSES["PLANK_HIGH"])
reg("ARCHER_B", POSES["PUSHUP_DOWN"])
reg("PSEUDO_PLANCHE", solve(lambda t: build(90 + t, 345, 345, 270 + t, 270 + t, 20, head=70 + t), 0, 45, HAND, TOE))
reg("KNEE_PLANK", solve(lambda t: build(90 + t, 0, 0, 270 + t, 240, 240, head=65 + t), 0, 45, HAND, KNEE))
reg("KNEE_DOWN", solve(lambda t: build(90 + t, 235, 0, 270 + t, 240, 240, head=65 + t), 0, 45, HAND, KNEE))
reg("PLANK_FOREARM", solve(lambda t: build(90 + t, 0, 90, 270 + t, 270 + t, 20, head=65 + t), 0, 45, HAND, TOE))
reg("SIDE_PLANK", solve(lambda t: build(90 + t, 0, 90, 270 + t, 270 + t, 20, head=90 + t,
                                       far={"arm": (180, 180)}), 0, 45, HAND, TOE))
reg("PIKE_UP", solve(lambda p: build(90 - p, 0, 0, 270 + p, 270 + p, head=90 - p), 15, 85, HAND, TOE))
_pu = 270 + 0  # legs of PIKE_UP are reused so only the upper body moves
_phi_up = POSES["PIKE_UP"]
_phi = None
for _p in range(15, 86):
    _pts, _ = build(90 - _p, 0, 0, 270 + _p, 270 + _p, head=90 - _p)
    if _pts is not None and abs(_pts[3][1] - _pts[7][1]) < 0.004:
        _phi = _p
        break
_phi = _phi or 60
reg("PIKE_DOWN", solve(lambda tq: build(tq, 235, 20, 270 + _phi, 270 + _phi, head=tq - 10), 20, 89, HAND, TOE))
reg("HANDSTAND", build(0, 0, 0, 180, 180, 180, head=0))

# ---------------------------------------------------------------- all fours family
def tabletop(head=None, far=None, thigh=0, shin=270):
    return solve(lambda t: build(90 + t, 0, 0, thigh, shin, 270, head=head if head is not None else 90 + t, far=far),
                 0, 60, HAND, KNEE)


reg("TABLETOP", tabletop())
reg("BIRD_A", tabletop())
reg("BIRD_B", tabletop(far={"arm": (90, 90), "leg": (270, 270), "foot": 270}))
reg("HYDRANT_A", tabletop())
reg("HYDRANT_B", tabletop(far={"leg": (90, 0), "foot": 90}))
reg("DONKEY_B", tabletop(far={"leg": (270, 180), "foot": 180}))
reg("CAT", tabletop(head=30))
reg("COW", tabletop(head=165))
reg("CHILD", build(80, 90, 90, 60, 270, 270, head=80))
reg("HAM_STRETCH", build(65, 80, 80, 90, 90, 180, head=70, far={"leg": (70, 290), "foot": 290}))

# ---------------------------------------------------------------- lying
reg("LYING", build(90, 270, 270, 270, 270, 180, head=90))
reg("CRUNCH_DOWN", build(90, 135, 18, 235, 300, 30, head=90))
reg("CRUNCH_UP", build(115, 135, 18, 235, 300, 30, head=115))
reg("LEGS_UP", build(90, 180, 180, 180, 270, 0, head=90))
reg("LEGS_LOW", build(90, 180, 180, 270, 270, 180, head=90, far={"arm": (90, 90), "leg": (180, 270), "foot": 0}))
reg("LEGS_STRAIGHT_UP", build(90, 270, 270, 180, 180, 90, head=90))
reg("REV_UP", build(82, 270, 270, 160, 265, 0, head=90))
reg("HOLLOW", build(112, 112, 112, 250, 250, 250, head=112))
reg("BRIDGE_DOWN", build(90, 270, 270, 235, 300, 30, head=90))
reg("BRIDGE_UP", build(70, 270, 270, 250, 0, 90, head=70))
reg("BRIDGE_EXT", build(70, 270, 270, 250, 250, 250, head=70))
reg("ROT_A", build(180, 0, 90, 0, 0, 90))
reg("ROT_B", build(180, 0, 55, 0, 0, 90))
reg("OVH_A", build(180, 180, 180, 0, 0, 90))
reg("OVH_B", build(180, 205, 205, 0, 0, 90))
reg("PULL_B", build(180, 70, 115, 0, 0, 90))
reg("SL_BRIDGE_UP", build(70, 270, 270, 250, 0, 90, head=70, far={"leg": (250, 250), "foot": 250}))
reg("SUPER_DOWN", build(90, 90, 90, 270, 270, 270, head=90))
reg("SUPER_UP", build(100, 110, 110, 255, 255, 255, head=105))
reg("PRONE_Y", build(100, 125, 125, 270, 270, 270, head=100))
reg("SIDE_LYING", build(90, 90, 90, 70, 290, 290, head=90))
reg("SIDE_CLAM", build(90, 90, 90, 70, 290, 290, head=90, far={"leg": (40, 290), "foot": 290}))

# ---------------------------------------------------------------- seated / skills
reg("L_SIT", build(180, 0, 0, 90, 90, 90, head=180))
reg("TUCK_SIT", build(180, 0, 0, 130, 20, 90, head=180))


# ================================================================= output
HEAD_R = 0.058


def extents(entry):
    pts, lift = entry
    xs = [p[0] for p in pts]
    ys = [p[1] for p in pts]
    top = min(min(ys), pts[0][1] - HEAD_R)
    height = (max(ys) + 0.015 - top) + lift
    width = max(xs) - min(xs) + 0.07
    return width, height


def fit(*entries):
    """Shared scale for the frames of one exercise (same rule as ExerciseDiagram.kt)."""
    mw = max(extents(e)[0] for e in entries)
    mh = max(extents(e)[1] for e in entries)
    return min(0.80 / mh, 0.88 / mw, 0.90)


def normalize(entry, s):
    pts, lift = entry
    xs = [p[0] for p in pts]
    ys = [p[1] for p in pts]
    cx = (min(xs) + max(xs)) / 2
    ymax = max(ys)
    return [(0.5 + (x - cx) * s, 0.90 + (y - ymax) * s - lift * s) for x, y in pts]


def kotlin():
    lines = [
        "package com.calisthenics.coach.data",
        "",
        "// GENERATED by tools/gen_poses.py - do not edit by hand.",
        "// Side-view joint positions (body-height units, hip at the origin) plus an airborne lift.",
        "object Poses {",
    ]
    for name, (pts, lift) in POSES.items():
        flat = ", ".join("%.4ff" % v for p in pts for v in p)
        lines.append("    val %s = Pose(floatArrayOf(%s), %.3ff)" % (name, flat, lift))
    lines.append("}")
    return "\n".join(lines) + "\n"


# ================================================================= preview rendering (mirrors the Kotlin drawing)
MUSCLE_SEGS = {
    "CHEST": {"TORSO", "UARM"}, "SHOULDERS": {"UARM"}, "TRICEPS": {"UARM"}, "BICEPS": {"UARM"},
    "FOREARMS": {"FARM"}, "BACK": {"TORSO"}, "LOWER_BACK": {"TORSO"}, "ABS": {"TORSO"}, "CORE": {"TORSO"},
    "QUADS": {"THIGH"}, "HAMSTRINGS": {"THIGH"}, "CALVES": {"SHIN"}, "GLUTES": {"THIGH", "HIP"},
    "HIPS": {"THIGH", "HIP"}, "FULL": {"TORSO", "UARM", "FARM", "THIGH", "SHIN"},
}


def render_panel(pose_name, muscle, size, band=None, s_fit=0.8):
    from PIL import Image, ImageDraw
    k = 2
    s = size * k
    img = Image.new("RGB", (s, s), (30, 41, 59))
    dr = ImageDraw.Draw(img)
    pts = [(x * s, y * s) for x, y in normalize(POSES[pose_name], s_fit)]
    body = (148, 163, 184)
    far_c = (90, 104, 124)
    accent = (34, 197, 94)
    far_acc = (24, 120, 66)
    hl = MUSCLE_SEGS.get(muscle, set())

    def seg(a, b, w, name, far=False):
        col = (far_acc if name in hl else far_c) if far else (accent if name in hl else body)
        dr.line([pts[a], pts[b]], fill=col, width=int(w * s))
        r = w * s / 2
        for p in (pts[a], pts[b]):
            dr.ellipse([p[0] - r, p[1] - r, p[0] + r, p[1] + r], fill=col)

    floor = 0.905 * s
    dr.line([(0.04 * s, floor), (0.96 * s, floor)], fill=(71, 85, 105), width=int(0.012 * s))
    # far limbs
    seg(1, 8, 0.040, "UARM", True)
    seg(8, 9, 0.034, "FARM", True)
    seg(4, 10, 0.060, "THIGH", True)
    seg(10, 11, 0.048, "SHIN", True)
    seg(11, 12, 0.030, "SHIN", True)
    # torso, near leg
    seg(4, 1, 0.085, "TORSO")
    if "HIP" in hl:
        r = 0.05 * s
        dr.ellipse([pts[4][0] - r, pts[4][1] - r, pts[4][0] + r, pts[4][1] + r], fill=accent)
    seg(4, 5, 0.066, "THIGH")
    seg(5, 6, 0.052, "SHIN")
    seg(6, 7, 0.032, "SHIN")
    # head, near arm
    r = 0.058 * s
    dr.line([pts[1], pts[0]], fill=body, width=int(0.03 * s))
    dr.ellipse([pts[0][0] - r, pts[0][1] - r, pts[0][0] + r, pts[0][1] + r], fill=body)
    seg(1, 2, 0.046, "UARM")
    seg(2, 3, 0.038, "FARM")
    if band is not None:
        orange = (245, 158, 11)
        if band == "KNEE":
            dr.line([pts[5], pts[10]], fill=orange, width=int(0.02 * s))
        elif band == "FOOT":
            dr.line([pts[6], pts[3]], fill=orange, width=int(0.014 * s))
        elif band == "HAND":
            dr.line([pts[3], pts[9]], fill=orange, width=int(0.02 * s))
        else:
            r2 = 0.07 * s
            c = pts[1]
            dr.ellipse([c[0] - r2, c[1] - r2, c[0] + r2, c[1] + r2], outline=orange, width=int(0.016 * s))
    return img.resize((size, size))


def parse_exercises(root):
    import glob
    src = "".join(open(f, encoding="utf-8").read() for f in sorted(glob.glob(root + "/data/Exercises*.kt")))
    out = []
    for m in re.finditer(
            r'ex\(\s*"(\w+)",\s*"([^"]*)",\s*"([^"]*)",\s*Muscle\.(\w+).*?Anim\(Poses\.(\w+),\s*Poses\.(\w+)(?:,\s*bandAt = Pose\.(\w+))?\)',
            src, re.S):
        out.append(m.groups())
    return out


def preview(outdir, root):
    from PIL import Image, ImageDraw
    import os
    os.makedirs(outdir, exist_ok=True)
    exs = parse_exercises(root)
    missing = sorted({n for e in exs for n in (e[4], e[5]) if n not in POSES})
    if missing:
        print("MISSING POSES:", missing)
    per = 12
    panel = 230
    for sheet in range(0, len(exs), per):
        chunk = exs[sheet:sheet + per]
        cols = 3
        rows = (len(chunk) + cols - 1) // cols
        W = cols * (2 * panel + 16)
        H = rows * (panel + 26)
        img = Image.new("RGB", (W, H), (15, 23, 42))
        dr = ImageDraw.Draw(img)
        for i, (eid, en, ar, muscle, a, b, band) in enumerate(chunk):
            x0 = (i % cols) * (2 * panel + 16)
            y0 = (i // cols) * (panel + 26)
            dr.text((x0 + 6, y0 + 4), "%s [%s]" % (eid, muscle), fill=(226, 232, 240))
            if a in POSES and b in POSES:
                sf = fit(POSES[a], POSES[b])
                img.paste(render_panel(a, muscle, panel, band, sf), (x0, y0 + 22))
                img.paste(render_panel(b, muscle, panel, band, sf), (x0 + panel + 4, y0 + 22))
        img.save("%s/sheet_%02d.png" % (outdir, sheet // per + 1))
    print(len(exs), "exercises rendered")


if __name__ == "__main__":
    import os
    here = os.path.dirname(os.path.abspath(__file__))
    root = os.path.join(here, "..", "app", "src", "main", "java", "com", "calisthenics", "coach")
    if sys.argv[1] == "kotlin":
        sys.stdout.write(kotlin())
    else:
        preview(sys.argv[2], root)
