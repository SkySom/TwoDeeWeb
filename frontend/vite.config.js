import {defineConfig} from "vite";
import scalaJSPlugin from "@scala-js/vite-plugin-scalajs";
import globResolverPlugin from "@raquo/vite-plugin-glob-resolver";
import importSideEffectPlugin from "@raquo/vite-plugin-import-side-effect";
import {fileURLToPath} from 'url';
import {dirname} from 'path';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

// Modified from https://github.com/raquo/laminar-full-stack-demo/blob/master/client/vite.config.js
export default defineConfig({
    base: "/",
    publicDir: "public",
    plugins: [
        scalaJSPlugin({
            cwd: "..",
            projectID: "frontend"
        }),
        globResolverPlugin({
            cwd: __dirname,
            ignore: [
                'node_modules/**',
                'target/**'
            ]
        }),
        importSideEffectPlugin({
            defNames: ['importStyle'],
            rewriteModuleIds: ['**/*.less', '**/*.css']
        })
    ],
    build: {
        outDir: "dist",
        assetsDir: "assets",
        cssCodeSplit: false,
        minify: "terser",
        sourcemap: true
    },
    server: {
        port: 3000,
        allowedHosts: ['relaxing-sponge-cuddly.ngrok-free.app'],
        strictPort: true,
        proxy: {
            "/api": {
                target: "http://127.0.0.1:8080",
                secure: false,
                configure: (proxy, _options) => {
                    proxy.on("error", (err, _req, _res) => {
                        console.log("proxy error", err);
                    });
                    proxy.on("proxyReq", (proxyReq, req, _res) => {
                        console.log("Sending Request to the Target:", req.method, req.url);
                    });
                    proxy.on("proxyRes", (proxyRes, req, _res) => {
                        console.log(
                            "Received Response from the Target:",
                            proxyRes.statusCode,
                            req.url
                        );
                    });
                },
            }
        }
    }
})