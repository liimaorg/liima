import { chromeLauncher } from '@web/test-runner';

export default {
  concurrency: 10,
  nodeResolve: true,
  watch: true,
  browsers: [chromeLauncher({ launchOptions: { args: ['--no-sandbox'] } })],
};
