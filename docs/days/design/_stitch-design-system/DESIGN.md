---
name: Two Hearts D-Day System
colors:
  surface: '#fff8f8'
  surface-dim: '#e5d6dc'
  surface-bright: '#fff8f8'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#fff0f5'
  surface-container: '#faeaef'
  surface-container-high: '#f4e4ea'
  surface-container-highest: '#eedfe4'
  on-surface: '#21191d'
  on-surface-variant: '#584145'
  inverse-surface: '#372e32'
  inverse-on-surface: '#fdedf2'
  outline: '#8c7074'
  outline-variant: '#dfbec3'
  surface-tint: '#b22252'
  primary: '#b02051'
  on-primary: '#ffffff'
  primary-container: '#d23c69'
  on-primary-container: '#fffeff'
  inverse-primary: '#ffb1c0'
  secondary: '#98462a'
  on-secondary: '#ffffff'
  secondary-container: '#fd9573'
  on-secondary-container: '#752c13'
  tertiary: '#aa2b55'
  on-tertiary: '#ffffff'
  tertiary-container: '#cb456d'
  on-tertiary-container: '#ffffff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffd9df'
  primary-fixed-dim: '#ffb1c0'
  on-primary-fixed: '#3f0016'
  on-primary-fixed-variant: '#90003c'
  secondary-fixed: '#ffdbd0'
  secondary-fixed-dim: '#ffb59e'
  on-secondary-fixed: '#3a0b00'
  on-secondary-fixed-variant: '#7a2f16'
  tertiary-fixed: '#ffd9df'
  tertiary-fixed-dim: '#ffb1c1'
  on-tertiary-fixed: '#3f0017'
  on-tertiary-fixed-variant: '#8b103f'
  background: '#fff8f8'
  on-background: '#21191d'
  surface-variant: '#eedfe4'
typography:
  display-hero:
    fontFamily: Plus Jakarta Sans
    fontSize: 44px
    fontWeight: '800'
    lineHeight: 52px
    letterSpacing: -0.03em
  display-hero-mobile:
    fontFamily: Plus Jakarta Sans
    fontSize: 36px
    fontWeight: '800'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 22px
    fontWeight: '700'
    lineHeight: 30px
    letterSpacing: -0.015em
  headline-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 24px
    letterSpacing: -0.01em
  body-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 17px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: -0.01em
  body-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 15px
    fontWeight: '400'
    lineHeight: 22px
    letterSpacing: -0.005em
  body-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
    letterSpacing: 0em
  label-lg:
    fontFamily: Plus Jakarta Sans
    fontSize: 15px
    fontWeight: '600'
    lineHeight: 20px
    letterSpacing: -0.005em
  label-md:
    fontFamily: Plus Jakarta Sans
    fontSize: 13px
    fontWeight: '600'
    lineHeight: 18px
    letterSpacing: 0em
  label-sm:
    fontFamily: Plus Jakarta Sans
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.02em
  metric-tabular:
    fontFamily: Plus Jakarta Sans
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 28px
    letterSpacing: -0.02em
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  gutter: 1rem
  gutter-compact: 0.75rem
  margin: 1.25rem
  margin-mobile: 1rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 0.875rem
  space-lg: 1.25rem
  space-xl: 1.75rem
  space-2xl: 2.5rem
---

## Brand & Style

The design system establishes an intimate, heartwarming, and refined emotional atmosphere for couple relationship tracking. The aesthetic balances warmth, romance, and digital clarity. 

- **Core Character**: Softly celebratory, clear, and reassuring. It avoids juvenile clutter in favor of an elegant, human-centered emotional dashboard.
- **Design Paradigm**: Tactile Soft Minimalism combined with iOS-native ergonomic clarity. Cards appear light and buoyant, micro-surfaces maintain high touch readability, and numerical milestones command presence without clinical stiffness.
- **Visual Weight**: Prioritize expansive white/pale-pink canvases, structured tabular counter metrics, subtle warmth-tinted boundaries, and intentional tactile taps.

## Colors

The palette uses a deliberate rose-coral spectrum engineered for high-legibility emotional tracking under pure light conditions.

- **Primary (`#D23C69`)**: Used for focal actions, milestone badges, hero counter highlights, and active selection anchors. Text overlay is strictly pure white (`#FFFFFF`).
- **Primary-Soft (`#FCE8EE`) & On-Primary-Soft (`#A62852`)**: Reserved for pill containers, status tags, and secondary action button states.
- **Accent Coral (`#F28C6B`) & Accent-Soft (`#FDEEE7`) / On-Accent-Soft (`#A8492C`)**: Used for secondary celebration dates (e.g., 100-day intervals, birthdays, anniversary tags).
- **Backgrounds & Surfaces**: Canvas root uses `#FBF8F9` (pale rose tint), elevated surfaces use `#FFFFFF`, and interactive wells/grouped controls use fill `#F5EFF2`.
- **Borders**: Subdued dividers use `#F0E6EA`; interactive card strokes and segment controls use `#DCCBD2`.
- **Text Hierarchy**: Primary content uses deep slate-tint `#221A1E`, supplementary metadata uses `#6E6268`, and placeholder/tertiary cues use `#968A90`.
- **Feedback Accents**: Error & destructive flows use `#C62828` / `#FDECEC`; positive and completed milestones use `#127235` / `#E7F5EC`.

## Typography

The typography structure combines clean, modern sans metrics with Korean Pretendard rendering standards, applying system fallback rules: `Pretendard, -apple-system, BlinkMacSystemFont, "Plus Jakarta Sans", sans-serif`.

- **Numeric Tabular Display**: All D-Day counters, elapsed days (`D+`, `D-`), timestamps, and countdown numbers must explicitly activate tabular numerals (`font-feature-settings: "tnum" 1`). This eliminates layout shift on fluctuating digits.
- **Milestone Scales**: Counter displays scale cleanly down to mobile viewports without wrapping, relying on `-0.02em` tracking for compactness.
- **Hierarchy Rhythm**: Primary headers employ confident weight distribution (700/800) to ground emotionally resonant phrases ("함께한 지 1,234일"), supported by calm, legible body weights (400) for journal entries and milestone titles.

## Layout & Spacing

The layout is built for single-hand mobile interactions, relying on vertical scroll streams and stacked horizontal glance cards.

- **Grid & Margins**: A single-column mobile fluid layout using a standard `margin-mobile` of `1rem` (16px) or `margin` of `1.25rem` (20px) on wider mobile screens. Cards span full inner width.
- **Vertical Spacing Rhythm**:
  - `space-xs` (4px): Micro-pairings, such as status dots next to text.
  - `space-sm` (8px): Metadata grouping, tag margins, label-to-input gap.
  - `space-md` (14px): Internal card cell padding, vertical input padding.
  - `space-lg` (20px): Gap between consecutive list items, card internal margin.
  - `space-xl` (28px) & `space-2xl` (40px): Section divisions between primary hero anniversary counters and upcoming milestone carousels.
- **Safe Area Insets**: Bottom sheets and interactive action bars preserve a minimum of `34px` bottom padding to accommodate native iOS Home indicators.

## Elevation & Depth

Visual hierarchy uses tonal surface layering combined with soft, warm-tinted ambient diffusion. Heavy black shadows are strictly prohibited.

- **Level 0 (Base Canvas)**: Background `#FBF8F9`. Completely flat.
- **Level 1 (Cards & Modules)**: Surface `#FFFFFF` bound by a crisp border stroke of `1px solid #F0E6EA` and an ambient rose shadow: `0px 4px 16px -2px rgba(210, 60, 105, 0.04)`.
- **Level 2 (Floating Action / Sticky Bars)**: Surface `#FFFFFF` or `rgba(255, 255, 255, 0.92)` with `backdrop-filter: blur(12px)`. Shadow: `0px 8px 24px -4px rgba(210, 60, 105, 0.08)`.
- **Level 3 (Modals & Bottom Drawers)**: Surface `#FFFFFF` with boundary stroke `#F0E6EA`. Shadow: `0px 16px 40px -8px rgba(34, 26, 30, 0.12)`.
- **Inset / Recessed Wells**: Background `#F5EFF2` with no shadow, utilizing flat contrast for segmented selectors, search bars, and date picker holders.

## Shapes

The design uses a balanced rounded approach (Level 2) that aligns with iOS Human Interface Guidelines while imparting warmth and softness.

- **Default Corners (`0.5rem` / 8px)**: Inputs, segmented button segments, tags, and small dialog alerts.
- **Large Corners (`1rem` / 16px)**: Milestone cards, profile picture holders, action bottom sheets, and journal entry blocks.
- **Extra-Large Corners (`1.5rem` / 24px)**: Hero celebration spotlight cards and photo containers.
- **Full Pill (`9999px`)**: Primary call-to-action buttons, filter chips, and D-Day badge indicators (e.g., `D-Day`, `D+500`).

## Components

### Buttons
- **Primary Action**: Full pill shape (`9999px`), solid `#D23C69` background, `#FFFFFF` bold label. Interactive press state scales to `0.98` with `#A62852`.
- **Soft Accent Action**: Full pill shape, `#FCE8EE` background with `#A62852` text. Used for secondary actions (e.g., "추억 추가하기").
- **Ghost Action**: Transparent background, text `#6E6268`, hover/active fill `#F5EFF2`.

### D-Day Badges & Chips
- **Live / Today Chip**: Background `#D23C69`, text `#FFFFFF`, tabular font weight 700.
- **Upcoming Milestone Chip**: Background `#FDEEE7`, text `#A8492C`, pill-shaped with `4px 10px` padding.
- **Passed Event Chip**: Background `#F5EFF2`, text `#6E6268`.

### Cards & Event Lists
- **Hero Counter Card**: `#FFFFFF` surface, `1.5rem` corner radius, `1px solid #F0E6EA` outline. Houses the primary couple avatar circle, live running anniversary counter with `display-hero-mobile`, and subtle progress ring.
- **Milestone List Item**: `#FFFFFF` surface with `1rem` corner radius. Contains date label on the left in tabular typography, event title in `#221A1E`, and D-Day badge pinned to the right. Separators inside groups use `#F0E6EA`.

### Form Controls & Inputs
- **Text Inputs & Date Pickers**: Height 48px, background `#FFFFFF`, border `1px solid #F0E6EA`, text `#221A1E`. Placeholder `#968A90`. Focused state switches border to `1.5px solid #D23C69` with an optional soft halo `rgba(210, 60, 105, 0.12)`.
- **Toggle Switch**: iOS-style pill. Inactive rail `#F0E6EA`, active rail `#D23C69`. Pure white circular thumb (`#FFFFFF`) with ambient drop shadow.
- **Checkboxes & Radios**: 20px round shape, checked state `#D23C69` with white check glyph, unchecked border `#DCCBD2`.

### Couple-Specific Modules
- **Anniversary Timeline Bar**: Segmented horizontal meter tracking the gap between major hundred-day milestones (e.g., 900일 to 1000일), rendered in `#FCE8EE` fill and filled with a `#D23C69` progress indicator.
- **Quick Reaction Float**: Rounded pill strip anchored above keyboard or bottom bar containing couple quick-emojis with `#F5EFF2` touch discs.