# setup.py
from distutils.core import setup
import py2exe,sys,os,pygame,livewires

origIsSystemDLL = py2exe.build_exe.isSystemDLL
def isSystemDLL(pathname):
        if os.path.basename(pathname).lower() in ("msvcp71.dll", "dwmapi.dll", "oealtu32.dll", "user32.dll", "imm32.dll", "shell32.dll", "ole32.dll",
													"comdlg32.dll", "comctl32.dll", "advap132.dll", "ws2_21.dll", "gd132.dll", "winmm.dll", "kernel32.dll", "sdl_ttf.dll", "libogg-0.dll"):
                return 0
        return origIsSystemDLL(pathname)
py2exe.build_exe.isSystemDLL = isSystemDLL

setup(windows=['breakaway.py'])