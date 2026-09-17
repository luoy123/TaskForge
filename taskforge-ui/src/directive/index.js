import hasPermi from './permission/hasPermi'

export default function setupDirectives(app) {
  app.directive('hasPermi', hasPermi)
}
