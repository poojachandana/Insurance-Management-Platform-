/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{js,jsx}",
  ],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#eef4ff',
          100: '#d9e6ff',
          200: '#bcd2ff',
          300: '#8fb4ff',
          400: '#5c8cff',
          500: '#3366ff',
          600: '#1f45e0',
          700: '#1a37b3',
          800: '#182f8f',
          900: '#182b73',
        },
      },
    },
  },
  plugins: [],
}
