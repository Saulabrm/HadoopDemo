using KafkaNet;
using KafkaNet.Model;
using KafkaNet.Protocol;
using System;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace KafkaTestClient
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
        }

        private async Task AddFileToKafka(byte[] filename, byte[] message, string topic)
        {            
            KafkaOptions options = new KafkaOptions(new Uri("http://sandbox.hortonworks.com:6667"));       
            using (BrokerRouter router = new BrokerRouter(options))
            using (Producer client = new Producer(router))
            {
                var topicMetas = router.GetTopicMetadata(topic);

                var responses = await client.SendMessageAsync(topic,
                    new[] {
                        new KafkaNet.Protocol.Message
                        {
                            Key = filename,
                            Value = message
                        }});                

                ProduceResponse response = responses.FirstOrDefault();
                MessageBox.Show(String.Format("File added to the queue - partition {0} offset {1}",
                    response.PartitionId,
                    response.Offset));
            }           
        }

        private void button1_Click(object sender, EventArgs e)
        {
            if (openFileDialog1.ShowDialog() == DialogResult.OK)
            {
                string filecontent = File.ReadAllText(openFileDialog1.FileName);

                this.AddFileToKafka(
                    Encoding.Default.GetBytes(openFileDialog1.FileName),
                    Encoding.Default.GetBytes(filecontent),
                    "stormwctopic");
            }
        }
    }
}
