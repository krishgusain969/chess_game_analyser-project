/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        chess: {
          dark: '#0b1f16',
          board: '#1f3a2a',
          wood: '#6b4f2a',
          accent: '#22c55e'
        }
      }
    }
  },
  plugins: []
};

