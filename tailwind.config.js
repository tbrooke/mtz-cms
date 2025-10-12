/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.clj",
    "./src/**/*.cljc",
    "./resources/**/*.html",
  ],
  theme: {
    extend: {
      // Custom color palette matching design-system.clj
      colors: {
        // Mount Zion brand colors
        'mtz-primary': {
          DEFAULT: '#2563eb', // blue-600
          light: '#dbeafe',   // blue-100
          lighter: '#eff6ff', // blue-50
          dark: '#1d4ed8',    // blue-700
          darker: '#1e40af',  // blue-800
        },
        'mtz-secondary': {
          DEFAULT: '#4b5563', // gray-600
          light: '#e5e7eb',   // gray-200
          lighter: '#f9fafb', // gray-50
        },
        // Status colors
        'mtz-success': {
          DEFAULT: '#15803d', // green-700
          light: '#bbf7d0',   // green-200
          bg: '#f0fdf4',      // green-50
        },
        'mtz-error': {
          DEFAULT: '#b91c1c', // red-700
          light: '#fca5a5',   // red-400
          bg: '#fef2f2',      // red-50
          border: '#fecaca',  // red-200
          strong: '#dc2626',  // red-600
        },
        'mtz-warning': {
          DEFAULT: '#a16207', // yellow-700
          light: '#fde047',   // yellow-300
          bg: '#fefce8',      // yellow-50
          dark: '#854d0e',    // yellow-800
        },
        'mtz-info': {
          DEFAULT: '#2563eb', // blue-600
          light: '#bfdbfe',   // blue-200
          bg: '#eff6ff',      // blue-50
        },
      },
      // Typography
      fontSize: {
        'xs': '0.75rem',     // 12px
        'sm': '0.875rem',    // 14px
        'base': '1rem',      // 16px
        'lg': '1.125rem',    // 18px
        'xl': '1.25rem',     // 20px
        '2xl': '1.5rem',     // 24px
        '3xl': '1.875rem',   // 30px
        '4xl': '2.25rem',    // 36px
        '5xl': '3rem',       // 48px
        '6xl': '3.75rem',    // 60px
      },
      // Spacing
      spacing: {
        'xs': '0.25rem',   // 4px
        'sm': '0.5rem',    // 8px
        'md': '1rem',      // 16px
        'lg': '1.5rem',    // 24px
        'xl': '2rem',      // 32px
        '2xl': '3rem',     // 48px
        '3xl': '4rem',     // 64px
        '4xl': '6rem',     // 96px
      },
      // Border radius
      borderRadius: {
        'none': '0',
        'sm': '0.125rem',  // 2px
        'md': '0.375rem',  // 6px
        'lg': '0.5rem',    // 8px
        'xl': '0.75rem',   // 12px
        'full': '9999px',
      },
      // Box shadows
      boxShadow: {
        'sm': '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
        'md': '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)',
        'lg': '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05)',
        'xl': '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04)',
        'none': 'none',
      },
      // Max widths for containers
      maxWidth: {
        '4xl': '56rem',     // 896px
        '6xl': '72rem',     // 1152px
        '7xl': '80rem',     // 1280px
        'prose': '65ch',
      },
      // Transition durations
      transitionDuration: {
        'fast': '150ms',
        'normal': '200ms',
        'slow': '300ms',
      },
      // Font weights
      fontWeight: {
        normal: '400',
        medium: '500',
        semibold: '600',
        bold: '700',
        extrabold: '800',
      },
    },
  },
  plugins: [
    // Add additional Tailwind plugins here if needed
    // Note: line-clamp is built into Tailwind CSS v3.3+
  ],
  // Safelist classes used dynamically in Clojure code
  safelist: [
    // Design system generated classes
    'text-blue-600',
    'text-blue-700',
    'text-blue-800',
    'bg-blue-600',
    'bg-blue-700',
    'bg-white',
    'bg-gray-50',
    'bg-gray-100',
    'bg-red-50',
    'bg-green-50',
    'bg-yellow-50',
    'border-gray-200',
    'border-red-200',
    'border-green-200',
    'border-yellow-300',
    'hover:bg-blue-700',
    'hover:text-blue-700',
    'hover:text-blue-800',
    'hover:shadow-lg',
    'hover:shadow-xl',
    // Spacing classes
    'px-1', 'px-2', 'px-4', 'px-6', 'px-8', 'px-12', 'px-16', 'px-24',
    'py-1', 'py-2', 'py-4', 'py-6', 'py-8', 'py-12', 'py-16', 'py-24',
    'p-1', 'p-2', 'p-4', 'p-6', 'p-8', 'p-12', 'p-16', 'p-24',
    'mb-1', 'mb-2', 'mb-4', 'mb-6', 'mb-8', 'mb-12', 'mb-16', 'mb-24',
    'mt-1', 'mt-2', 'mt-4', 'mt-6', 'mt-8', 'mt-12', 'mt-16', 'mt-24',
    'gap-1', 'gap-2', 'gap-4', 'gap-6', 'gap-8',
    // Rounded classes
    'rounded-none', 'rounded-sm', 'rounded-md', 'rounded-lg', 'rounded-xl', 'rounded-full',
    // Shadow classes
    'shadow-none', 'shadow-sm', 'shadow-md', 'shadow-lg', 'shadow-xl',
    // Text sizes
    'text-xs', 'text-sm', 'text-base', 'text-lg', 'text-xl', 'text-2xl', 'text-3xl', 'text-4xl', 'text-5xl', 'text-6xl',
    // Font weights
    'font-normal', 'font-medium', 'font-semibold', 'font-bold', 'font-extrabold',
    // Transitions
    'transition', 'transition-all', 'transition-colors', 'transition-opacity', 'transition-shadow', 'transition-transform',
    'duration-150', 'duration-200', 'duration-300',
  ],
}
