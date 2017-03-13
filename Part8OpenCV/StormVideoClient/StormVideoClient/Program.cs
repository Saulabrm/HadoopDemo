using Storm.DRPC;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace StormVideoClient
{
    class Program
    {
        static void Main(string[] args)
        {
            var bytes = File.ReadAllBytes(@"D:\Projects\drive-download-20170311T100820Z-001\ffmpeg-20161110-872b358-win64-static\bin\out1.mp4");
            string base64Str = Convert.ToBase64String(bytes);

            DRPCClient client = new DRPCClient("localhost", 3772);
            string result = client.execute("video", base64Str);

            File.WriteAllBytes("tmp.avi", Convert.FromBase64String(result));
        }
    }
}
