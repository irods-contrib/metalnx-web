import os


class MetalnxContext:
    def __init__(self):
        pass

    def banner(self):
        main_line = '#          Metalnx Installation Script        #'
        return '#' * len(main_line) + '\n' + main_line + '\n' + '#' * len(main_line)

    def config_java_devel(self):
        '''The installation process will make sure the java-devel package is correctly installed'''
        os.stat('/usr/bin/jar')
        return True

    def run(self):
        '''
        Runs Metalnx configuration
        '''
        print self.banner()

        # Filtering out method that does not start with 'config_'
        methods_to_run = [m for m in dir(self) if m.startswith('config_')]

        for step, method in enumerate(methods_to_run):
            invokable = getattr(self, method)
            print '[*] Executing {} ({}/{})\n   - {}'.format(method, step + 1, len(methods_to_run), invokable.__doc__)

            try:
                invokable()
            except Exception as e:
                print '[ERROR]: {}'.format(e)


def main():
    MetalnxContext().run()


if __name__ == '__main__':
    main()
