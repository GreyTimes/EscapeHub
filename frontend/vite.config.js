import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
    plugins: [react()],

    // Read the shared .env file from the repository root.
    envDir: '..',

    server: {
        host: 'localhost',
        port: 5173,
        strictPort: true
    }
});
