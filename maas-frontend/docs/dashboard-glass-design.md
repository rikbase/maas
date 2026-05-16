# Dashboard Glass Redesign

> Transform the Dashboard into a glassmorphism-styled, dynamic, three-dimensional interface with rich micro-interactions.

## Color Palette

- **Page Background**: Dynamic gradient cycling through `#0f0414` (deep purple) → `#0f0b29` (indigo) → `#061412` (teal) over 20s
- **Glass Cards**: `rgba(255,255,255,0.06)` fill, `backdrop-filter: blur(24px)`, `border: 1px solid rgba(255,255,255,0.08)`
- **Text**: White/light with varying opacity for hierarchy

## Layout Sections

### 1. KPI Bar — Floating Glass Capsules
- Horizontal row of pill-shaped glass capsules (`border-radius: 20px`)
- Each shows only the primary count (active/online), large bold font
- Left-side colored glow dot per category
- Hover: translateY(-2px) + glow expansion
- Same click navigation as current

### 2. Usage Cards — 2×2 Large Glass Cards
- 4 cards in a 2×2 grid
- Top gradient border strip (replacing current left border)
- Icon + count + label vertically stacked
- Hover: translateY(-4px) + shadow spread + icon subtle rotation
- Entry: staggered fade-in-up from bottom

### 3. Login Activity — Glass Panel (right column)
- Same glass styling as usage cards
- Remains in right column alongside usage grid

### 4. Trend Charts — Glass Panels
- Two side-by-side glass cards
- Gradient glass columns with backdrop-filter
- Colored segments for execution stacked bars
- Hover: bar lift + glow

### 5. Data Tables — Glass Tables
- Glass card container
- Semi-transparent dark header row
- Row hover: white semi-transparent overlay
- Glass rank badges with glow borders
- Health dots with glow box-shadow

## Interactions

- **Page entry**: Sequential fadeInUp for each section group, ~80ms stagger
- **useCountUp**: Keep existing animated counters
- **Hover**: All glass cards lift 4px + shadow spread + border brighten
- **Mouse-tracked glow**: Single radial-gradient pseudo-element following cursor across page, adding dynamic light
- **Value pulse**: Quick yellow flash on usage numbers when data updates
- **Background**: Slow 20s gradient animation

## Implementation Notes

- All changes confined to `Dashboard.vue` (scoped styles + template)
- No new dependencies, no external CSS framework
- Use existing CSS variables from `variables.css`
- Background animation via `@keyframes` on `.dashboard` or `:root`
